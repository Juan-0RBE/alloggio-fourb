package co.edu.unbosque.alloggio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import co.edu.unbosque.alloggio.dto.StripeRespuestaDTO;
import co.edu.unbosque.alloggio.util.exception.PasarelaException;

@Component
public class ClienteHTTP {

    private static final Logger log = LoggerFactory.getLogger(ClienteHTTP.class);

    /** Código y cuerpo crudo de una respuesta HTTP. */
    private record RespuestaHttp(int codigo, String cuerpo) {
    }

    private final RestClient restClient;
    private final String apiKey;

    public ClienteHTTP(@Value("${stripe.api-url:https://api.stripe.com/v1}") String apiUrl,
            @Value("${stripe.api-key:}") String apiKey) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(5000);
        fabrica.setReadTimeout(20000);
        this.restClient = RestClient.builder().baseUrl(apiUrl).requestFactory(fabrica).build();
        this.apiKey = apiKey;
    }

    /**
     * Crea y confirma un cobro (PaymentIntent) en una sola llamada, sin webhooks.
     * 
     * @param montoUnidadMenormoneda
     * @param moneda
     * @param metodoPago
     * @param referencia
     * @param claveIdempotencia
     * @return
     * @throws PasarelaException
     */
    public StripeRespuestaDTO crearCobro(long montoUnidadMenor, String moneda, String metodoPago,
            String referencia, String claveIdempotencia) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("amount", String.valueOf(montoUnidadMenor));
        form.add("currency", moneda.toLowerCase());
        form.add("payment_method", metodoPago);
        form.add("confirm", "true");
        form.add("automatic_payment_methods[enabled]", "true");
        form.add("automatic_payment_methods[allow_redirects]", "never");
        form.add("metadata[referencia]", referencia);
        form.add("description", "Alloggio " + referencia);
        form.add("expand[]", "latest_charge");

        RespuestaHttp respuesta = enviar("/payment_intents", form, claveIdempotencia);
        JsonObject raiz = leerJson(respuesta.cuerpo());

        JsonObject error = objeto(raiz, "error");
        if (error != null) {
            // Rechazo de tarjeta: es un resultado de negocio, no una falla técnica
            if (respuesta.codigo() == 402 && "card_error".equals(texto(error, "type"))) {
                StripeRespuestaDTO dto = new StripeRespuestaDTO();
                dto.setCodigoHttp(respuesta.codigo());
                JsonObject intento = objeto(error, "payment_intent");
                dto.setId(texto(intento, "id"));
                dto.setEstado(texto(intento, "status"));
                dto.setCodigoError(texto(error, "code"));
                dto.setCodigoRechazo(texto(error, "decline_code"));
                dto.setMensajeError(texto(error, "message"));
                return dto;
            }
            throw new PasarelaException(
                    "Stripe devolvió un error (" + texto(error, "type") + "/" + texto(error, "code") + ")");
        }
        if (respuesta.codigo() < 200 || respuesta.codigo() >= 300) {
            throw new PasarelaException("Stripe respondió con HTTP " + respuesta.codigo());
        }
        return mapearCobro(raiz, respuesta.codigo());
    }

    /**
     * Reembolsa un cobro ya aprobado.
     * 
     * @param referenciaCobro
     * @param montoUnidadMenor
     * @param claveIdempotencia
     * @return
     * @throws PasarelaException
     */
    public StripeRespuestaDTO reembolsar(String referenciaCobro, long montoUnidadMenor, String claveIdempotencia) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("payment_intent", referenciaCobro);
        form.add("amount", String.valueOf(montoUnidadMenor));

        RespuestaHttp respuesta = enviar("/refunds", form, claveIdempotencia);
        JsonObject raiz = leerJson(respuesta.cuerpo());

        JsonObject error = objeto(raiz, "error");
        if (error != null) {
            throw new PasarelaException(
                    "Stripe devolvió un error (" + texto(error, "type") + "/" + texto(error, "code") + ")");
        }
        if (respuesta.codigo() < 200 || respuesta.codigo() >= 300) {
            throw new PasarelaException("Stripe respondió con HTTP " + respuesta.codigo());
        }
        StripeRespuestaDTO dto = new StripeRespuestaDTO();
        dto.setCodigoHttp(respuesta.codigo());
        dto.setId(texto(raiz, "id"));
        dto.setEstado(texto(raiz, "status"));
        dto.setMoneda(texto(raiz, "currency"));
        dto.setMonto(numero(raiz, "amount"));
        return dto;
    }

    /** Convierte un PaymentIntent exitoso en StripeRespuestaDTO. */
    private StripeRespuestaDTO mapearCobro(JsonObject intento, int codigoHttp) {
        StripeRespuestaDTO dto = new StripeRespuestaDTO();
        dto.setCodigoHttp(codigoHttp);
        dto.setId(texto(intento, "id"));
        dto.setEstado(texto(intento, "status"));
        dto.setMoneda(texto(intento, "currency"));
        dto.setMonto(numero(intento, "amount"));

        // latest_charge viene como objeto porque se pidió expand[]=latest_charge
        JsonObject cargo = objeto(intento, "latest_charge");
        JsonObject detalle = objeto(cargo, "payment_method_details");
        JsonObject tarjeta = objeto(detalle, "card");
        dto.setMarcaTarjeta(texto(tarjeta, "brand"));
        dto.setUltimosCuatro(texto(tarjeta, "last4"));

        // Si no quedó pagado (por ejemplo, requires_action), Stripe puede traer el
        // motivo
        JsonObject errorPago = objeto(intento, "last_payment_error");
        if (errorPago != null) {
            dto.setCodigoError(texto(errorPago, "code"));
            dto.setCodigoRechazo(texto(errorPago, "decline_code"));
            dto.setMensajeError(texto(errorPago, "message"));
        }
        return dto;
    }

    /**
     * Envía un POST con formulario a Stripe.
     */
    private RespuestaHttp enviar(String ruta, MultiValueMap<String, String> form, String claveIdempotencia) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new PasarelaException("Falta configurar la clave de Stripe (stripe.api-key / STRIPE_API_KEY)");
        }
        try {
            return restClient.post().uri(ruta).headers(h -> {
                h.setBearerAuth(apiKey);
                h.set("Idempotency-Key", claveIdempotencia);
            }).contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
                    .exchange((peticion, respuesta) -> new RespuestaHttp(respuesta.getStatusCode().value(),
                            respuesta.bodyTo(String.class)));
        } catch (RestClientException e) {
            // Solo el tipo de excepción: el mensaje podría contener datos de la petición
            log.error("No fue posible comunicarse con Stripe ({})", e.getClass().getSimpleName());
            throw new PasarelaException("No fue posible comunicarse con Stripe", e);
        }
    }

    private JsonObject leerJson(String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            throw new PasarelaException("Stripe devolvió una respuesta vacía");
        }
        try {
            JsonElement elemento = JsonParser.parseString(cuerpo);
            if (!elemento.isJsonObject()) {
                throw new PasarelaException("Stripe devolvió una respuesta con formato inesperado");
            }
            return elemento.getAsJsonObject();
        } catch (JsonParseException e) {
            throw new PasarelaException("Stripe devolvió una respuesta que no es JSON válido", e);
        }
    }

    /**
     * Lee un campo de texto o número como String; null si no existe o no es simple.
     */
    private static String texto(JsonObject objeto, String campo) {
        if (objeto == null) {
            return null;
        }
        JsonElement e = objeto.get(campo);
        return (e == null || !e.isJsonPrimitive()) ? null : e.getAsString();
    }

    /** Lee un campo numérico entero; null si no existe. */
    private static Long numero(JsonObject objeto, String campo) {
        String valor = texto(objeto, campo);
        return valor == null ? null : Long.valueOf(valor);
    }

    /** Lee un campo como objeto JSON; null si no existe o no es un objeto. */
    private static JsonObject objeto(JsonObject objeto, String campo) {
        if (objeto == null) {
            return null;
        }
        JsonElement e = objeto.get(campo);
        return (e != null && e.isJsonObject()) ? e.getAsJsonObject() : null;
    }

}
