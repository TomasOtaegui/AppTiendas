package com.stt.apptiendas;

import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TiendaIntentsTest {
    @Test
    public void enlacesHttpsSonImplicitos() {
        Intent web = TiendaIntents.web("ejemplo.com/tienda");
        assertEquals(Intent.ACTION_VIEW, web.getAction());
        assertEquals("https://ejemplo.com/tienda", web.getDataString());
        assertNull(web.getComponent());
        assertNull(web.getPackage());
        assertEquals("https://instagram.com/tienda", TiendaIntents.web("https://instagram.com/tienda").getDataString());
    }
    @Test
    public void urlsInvalidasSeRechazan() {
        String[] invalidas = {"", "hola", "javascript:alert(1)", "https://", "https://sitio con espacios.cl"};
        for (String url : invalidas) assertThrows(IllegalArgumentException.class, () -> TiendaIntents.web(url));
    }
    @Test
    public void telefonoAbreElMarcadorSinLlamar() {
        Intent telefono = TiendaIntents.telefono("+56 (9) 1234-5678");
        assertEquals(Intent.ACTION_DIAL, telefono.getAction());
        assertEquals("tel", telefono.getData().getScheme());
        assertEquals("+56912345678", telefono.getData().getSchemeSpecificPart());
        assertNull(telefono.getComponent());
        assertThrows(IllegalArgumentException.class, () -> TiendaIntents.telefono("abc"));
    }
    @Test
    public void mapaCodificaNombreYAdmiteComaDecimal() {
        Intent mapa = TiendaIntents.mapa("-33,4489", "-70.6693", "Café & Pan #1");
        assertEquals(Intent.ACTION_VIEW, mapa.getAction());
        assertEquals("geo", mapa.getData().getScheme());
        assertTrue(mapa.getDataString().contains("%26"));
        assertNull(mapa.getComponent());
        assertEquals("https://maps.google.com/?q=-33.4489,-70.6693", TiendaIntents.enlaceMapa("-33.4489", "-70.6693"));
    }
    @Test
    public void coordenadasFueraDeRangoONoNumericasSeRechazan() {
        assertThrows(IllegalArgumentException.class, () -> TiendaIntents.mapa("91", "0", "Tienda"));
        assertThrows(IllegalArgumentException.class, () -> TiendaIntents.mapa("0", "181", "Tienda"));
        assertThrows(IllegalArgumentException.class, () -> TiendaIntents.mapa("NaN", "0", "Tienda"));
        assertThrows(IllegalArgumentException.class, () -> TiendaIntents.mapa("0", "Infinity", "Tienda"));
        assertThrows(IllegalArgumentException.class, () -> TiendaIntents.mapa("", "0", "Tienda"));
    }
    @Test
    public void smsPreparaDestinatarioYMensaje() {
        Intent sms = TiendaIntents.sms("", "Tienda\nWeb: https://ejemplo.com");
        assertEquals(Intent.ACTION_SENDTO, sms.getAction());
        assertEquals("smsto:", sms.getDataString());
        assertEquals("Tienda\nWeb: https://ejemplo.com", sms.getStringExtra("sms_body"));
        assertNull(sms.getComponent());
        assertEquals("smsto:+56912345678", TiendaIntents.sms("+56 9 1234 5678", "Hola").getDataString());
    }
}
