package com.stt.apptiendas;

import android.content.Intent;
import android.net.Uri;
import java.net.URI;
import java.net.URISyntaxException;

/** Construcción y validación de intents externos, independiente de la interfaz. */
public final class TiendaIntents {
    private TiendaIntents() { }

    public static Intent web(String entrada) {
        String url = entrada.trim();
        if (!url.contains("://")) url = "https://" + url;
        try {
            URI validada = new URI(url);
            String host = validada.getHost();
            if (!"https".equalsIgnoreCase(validada.getScheme()) || host == null
                    || !host.contains(".") || validada.getUserInfo() != null) {
                throw new IllegalArgumentException("URL HTTPS inválida");
            }
            return new Intent(Intent.ACTION_VIEW, Uri.parse(validada.toASCIIString()));
        } catch (URISyntaxException e) { throw new IllegalArgumentException("URL inválida", e); }
    }
    public static String normalizarTelefono(String entrada) {
        String numero = entrada.trim().replaceAll("[\\s().-]", "");
        if (!numero.matches("\\+?[0-9]{3,15}")) throw new IllegalArgumentException("Número inválido");
        return numero;
    }
    public static Intent telefono(String numero) {
        return new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", normalizarTelefono(numero), null));
    }
    private static String coordenadas(String latitud, String longitud) {
        try {
            double lat = Double.parseDouble(latitud.trim().replace(',', '.'));
            double lon = Double.parseDouble(longitud.trim().replace(',', '.'));
            if (!Double.isFinite(lat) || !Double.isFinite(lon) || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                throw new IllegalArgumentException("Coordenadas fuera de rango");
            }
            return lat + "," + lon;
        } catch (NumberFormatException e) { throw new IllegalArgumentException("Coordenadas inválidas", e); }
    }
    public static Intent mapa(String latitud, String longitud, String nombre) {
        String posicion = coordenadas(latitud, longitud);
        String etiqueta = nombre.trim().isEmpty() ? "Tienda" : nombre.trim();
        return new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + posicion + "?q="
                + Uri.encode(posicion + "(" + etiqueta + ")")));
    }
    public static String enlaceMapa(String latitud, String longitud) {
        return "https://maps.google.com/?q=" + coordenadas(latitud, longitud);
    }
    public static Intent sms(String destinatario, String mensaje) {
        String numero = destinatario.trim().isEmpty() ? "" : normalizarTelefono(destinatario);
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + numero));
        intent.putExtra("sms_body", mensaje);
        return intent;
    }
}
