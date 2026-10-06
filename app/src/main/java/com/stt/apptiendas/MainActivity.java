package com.stt.apptiendas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ClipData;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private EditText nombre, telefono, web, instagram, latitud, longitud, destinatarioSms;
    private TextView estadoFoto, estadoUbicacion;
    private Button btnTomarFoto, btnVerFoto, btnUbicacion;
    private Uri fotoUri, fotoPendiente;
    private boolean abrirMapaDespues;
    private CancellationSignal solicitudUbicacion;
    private AnimatedImageDrawable animacionTienda;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeoutUbicacion = () -> {
        cancelarUbicacion();
        abrirMapaDespues = false;
        estadoUbicacion.setText(R.string.ubicacion_no_disponible);
        aviso(R.string.ubicacion_no_disponible);
    };

    // Con EXTRA_OUTPUT, la cámara puede devolver RESULT_OK sin un Intent de datos.
    private final ActivityResultLauncher<Intent> camara = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
        btnTomarFoto.setEnabled(true);
        Log.d("AppTiendas", "Resultado de cámara: " + resultado.getResultCode());
        if (resultado.getResultCode() != Activity.RESULT_OK || fotoPendiente == null) {
            eliminarFotoPendiente();
            aviso(R.string.foto_cancelada);
        } else {
            finalizarFoto();
        }
    });

    private final ActivityResultLauncher<String[]> permisos = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
        if (tieneUbicacion()) obtenerUbicacion();
        else {
            abrirMapaDespues = false;
            estadoUbicacion.setText(R.string.ubicacion_permiso_denegado);
            aviso(R.string.ubicacion_permiso_denegado);
        }
    });

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        cargarAnimacionTienda();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
        nombre = findViewById(R.id.et_nombre);
        telefono = findViewById(R.id.et_telefono);
        web = findViewById(R.id.et_web);
        instagram = findViewById(R.id.et_instagram);
        latitud = findViewById(R.id.et_latitud);
        longitud = findViewById(R.id.et_longitud);
        destinatarioSms = findViewById(R.id.et_destinatario_sms);
        estadoFoto = findViewById(R.id.tv_estado_foto);
        estadoUbicacion = findViewById(R.id.tv_estado_ubicacion);
        btnTomarFoto = findViewById(R.id.btn_tomar_foto);
        btnVerFoto = findViewById(R.id.btn_ver_foto);
        btnUbicacion = findViewById(R.id.btn_ubicacion_actual);
        if (estado != null) {
            fotoUri = uriGuardada(estado, "foto_uri");
            fotoPendiente = uriGuardada(estado, "foto_pendiente");
            abrirMapaDespues = estado.getBoolean("abrir_mapa");
        }
        actualizarFoto();
        btnTomarFoto.setEnabled(fotoPendiente == null);
        btnTomarFoto.setOnClickListener(v -> tomarFoto());
        findViewById(R.id.btn_web).setOnClickListener(v -> abrirEnlace(web));
        findViewById(R.id.btn_instagram).setOnClickListener(v -> abrirEnlace(instagram));
        findViewById(R.id.btn_llamar).setOnClickListener(v -> abrirTelefono());
        findViewById(R.id.btn_mapa).setOnClickListener(v -> abrirMapa());
        btnUbicacion.setOnClickListener(v -> solicitarUbicacion(false));
        findViewById(R.id.btn_sms).setOnClickListener(v -> compartirSms());

        // Se conservan los tres intents explícitos y sus claves originales.
        findViewById(R.id.btn_ajustes).setOnClickListener(v ->
                startActivity(new Intent(this, ConfigActivity.class)));
        findViewById(R.id.btn_resumen).setOnClickListener(v -> {
            Intent detalle = new Intent(this, DetalleActivity.class);
            detalle.putExtra("NOMBRE_TIENDA", texto(nombre));
            detalle.putExtra("WEB_TIENDA", texto(web));
            detalle.putExtra("TELEFONO_TIENDA", texto(telefono));
            detalle.putExtra("INSTAGRAM_TIENDA", texto(instagram));
            detalle.putExtra("LATITUD_TIENDA", texto(latitud));
            detalle.putExtra("LONGITUD_TIENDA", texto(longitud));
            if (fotoUri != null) detalle.putExtra("FOTO_URI", fotoUri.toString());
            startActivity(detalle);
        });
        btnVerFoto.setOnClickListener(v -> {
            if (fotoUri == null) { aviso(R.string.foto_primero); return; }
            Intent foto = new Intent(this, PhotoActivity.class);
            foto.putExtra("FOTO_URI", fotoUri.toString());
            startActivity(foto);
        });
    }

    // GIF local, compatible con el minSdk 31 y sin dependencias adicionales.
    private void cargarAnimacionTienda() {
        ImageView tienda = findViewById(R.id.iv_tiendita);
        try {
            Drawable imagen = ImageDecoder.decodeDrawable(
                    ImageDecoder.createSource(getResources(), R.raw.tiendita_animada));
            tienda.setImageDrawable(imagen);
            if (imagen instanceof AnimatedImageDrawable) {
                animacionTienda = (AnimatedImageDrawable) imagen;
                animacionTienda.setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
            }
        } catch (IOException | Resources.NotFoundException e) {
            // El XML conserva una imagen estática si el GIF no se puede leer.
            Log.w("AppTiendas", "No se pudo cargar la animación de la tienda", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (animacionTienda != null) animacionTienda.start();
    }

    // Implícito: captura completa, guardada en la galería mediante MediaStore.
    private void tomarFoto() {
        try {
            ContentValues datos = new ContentValues();
            datos.put(MediaStore.Images.Media.DISPLAY_NAME, "Tienda_" + System.currentTimeMillis() + ".jpg");
            datos.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            datos.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AppTiendas");
            // La cámara externa necesita acceder al registro: IS_PENDING=1 lo limita al dueño.
            // Si se cancela la captura, se elimina el registro creado por esta app.
            fotoPendiente = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, datos);
            if (fotoPendiente == null) throw new IOException("No se pudo crear la imagen");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoPendiente);
            intent.setClipData(ClipData.newRawUri("Foto de la tienda", fotoPendiente));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            btnTomarFoto.setEnabled(false);
            camara.launch(intent);
        } catch (ActivityNotFoundException e) {
            eliminarFotoPendiente(); btnTomarFoto.setEnabled(true); aviso(R.string.camara_no_disponible);
        } catch (IOException | SecurityException | IllegalArgumentException e) {
            Log.w("AppTiendas", "No se pudo iniciar la captura", e);
            eliminarFotoPendiente(); btnTomarFoto.setEnabled(true); aviso(R.string.foto_error);
        }
    }

    private void finalizarFoto() {
        try (InputStream entrada = getContentResolver().openInputStream(fotoPendiente)) {
            BitmapFactory.Options dimensiones = new BitmapFactory.Options();
            dimensiones.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(entrada, null, dimensiones);
            if (dimensiones.outWidth <= 0 || dimensiones.outHeight <= 0) throw new IOException("Foto inválida");
            fotoUri = fotoPendiente;
            fotoPendiente = null;
            actualizarFoto();
            aviso(R.string.foto_guardada);
            if (texto(latitud).isEmpty() && texto(longitud).isEmpty()) solicitarUbicacion(false);
        } catch (IOException | SecurityException | IllegalArgumentException e) {
            Log.w("AppTiendas", "No se pudo guardar la captura", e);
            eliminarFotoPendiente(); aviso(R.string.foto_error);
        }
    }

    private void eliminarFotoPendiente() {
        if (fotoPendiente == null) return;
        try { getContentResolver().delete(fotoPendiente, null, null); }
        catch (SecurityException | IllegalArgumentException ignored) { /* Puede haber sido retirada. */ }
        fotoPendiente = null;
    }

    // Implícito: web e Instagram son destinos de la misma acción ACTION_VIEW.
    private void abrirEnlace(EditText campo) {
        try { lanzar(TiendaIntents.web(texto(campo)), R.string.navegador_no_disponible); }
        catch (IllegalArgumentException e) {
            campo.setError(getString(R.string.url_invalida)); campo.requestFocus();
        }
    }

    // Implícito: se abre el marcador con el número, sin llamar automáticamente.
    private void abrirTelefono() {
        try { lanzar(TiendaIntents.telefono(texto(telefono)), R.string.telefono_no_disponible); }
        catch (IllegalArgumentException e) {
            telefono.setError(getString(R.string.telefono_invalido)); telefono.requestFocus();
        }
    }

    // Implícito: mapa con coordenadas obtenidas o introducidas manualmente.
    private void abrirMapa() {
        if (texto(latitud).isEmpty() && texto(longitud).isEmpty()) { solicitarUbicacion(true); return; }
        try { lanzar(TiendaIntents.mapa(texto(latitud), texto(longitud), texto(nombre)), R.string.mapa_no_disponible); }
        catch (IllegalArgumentException e) {
            latitud.setError(getString(R.string.coordenadas_invalidas));
            longitud.setError(getString(R.string.coordenadas_invalidas)); latitud.requestFocus();
        }
    }

    // Implícito: prepara un SMS. El envío queda en manos del usuario.
    private void compartirSms() {
        if (texto(nombre).isEmpty()) {
            nombre.setError(getString(R.string.nombre_necesario)); nombre.requestFocus(); return;
        }
        try {
            StringBuilder mensaje = new StringBuilder("Te recomiendo esta tienda: ").append(texto(nombre));
            if (!texto(telefono).isEmpty()) mensaje.append("\nTeléfono: ").append(TiendaIntents.normalizarTelefono(texto(telefono)));
            if (!texto(web).isEmpty()) mensaje.append("\nWeb: ").append(TiendaIntents.web(texto(web)).getData());
            if (!texto(instagram).isEmpty()) mensaje.append("\nInstagram: ").append(TiendaIntents.web(texto(instagram)).getData());
            if (!texto(latitud).isEmpty() || !texto(longitud).isEmpty()) mensaje.append("\nUbicación: ").append(TiendaIntents.enlaceMapa(texto(latitud), texto(longitud)));
            lanzar(TiendaIntents.sms(texto(destinatarioSms), mensaje.toString()), R.string.sms_no_disponible);
        } catch (IllegalArgumentException e) { aviso(R.string.datos_sms_invalidos); }
    }

    private void lanzar(Intent intent, int sinApp) {
        // No requiere consultar paquetes: se controla el error si no hay aplicación compatible.
        try { startActivity(intent); }
        catch (ActivityNotFoundException e) { aviso(sinApp); }
        catch (SecurityException e) { aviso(R.string.accion_no_permitida); }
    }

    private boolean tieneUbicacion() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void solicitarUbicacion(boolean abrirMapa) {
        abrirMapaDespues = abrirMapa;
        if (tieneUbicacion()) obtenerUbicacion();
        else permisos.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    // El permiso se comprueba antes de llamar aquí y en la respuesta del diálogo.
    @SuppressLint("MissingPermission")
    private void obtenerUbicacion() {
        cancelarUbicacion();
        LocationManager gestor = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            if (gestor == null || !gestor.isLocationEnabled()) {
                estadoUbicacion.setText(R.string.ubicacion_desactivada);
                aviso(R.string.ubicacion_desactivada); abrirMapaDespues = false; return;
            }
            boolean precisa = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            String proveedor = null;
            if (gestor.hasProvider(LocationManager.FUSED_PROVIDER) && gestor.isProviderEnabled(LocationManager.FUSED_PROVIDER)) proveedor = LocationManager.FUSED_PROVIDER;
            else if (precisa && gestor.hasProvider(LocationManager.GPS_PROVIDER) && gestor.isProviderEnabled(LocationManager.GPS_PROVIDER)) proveedor = LocationManager.GPS_PROVIDER;
            else if (gestor.hasProvider(LocationManager.NETWORK_PROVIDER) && gestor.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) proveedor = LocationManager.NETWORK_PROVIDER;
            if (proveedor == null) {
                estadoUbicacion.setText(R.string.ubicacion_no_disponible);
                aviso(R.string.ubicacion_no_disponible); abrirMapaDespues = false; return;
            }
            solicitudUbicacion = new CancellationSignal();
            btnUbicacion.setEnabled(false);
            estadoUbicacion.setText(R.string.ubicacion_buscando);
            handler.postDelayed(timeoutUbicacion, 15000);
            gestor.getCurrentLocation(proveedor, solicitudUbicacion, getMainExecutor(), ubicacion -> {
                handler.removeCallbacks(timeoutUbicacion);
                solicitudUbicacion = null; btnUbicacion.setEnabled(true);
                if (ubicacion == null) {
                    estadoUbicacion.setText(R.string.ubicacion_no_disponible);
                    aviso(R.string.ubicacion_no_disponible); abrirMapaDespues = false; return;
                }
                latitud.setText(String.valueOf(ubicacion.getLatitude()));
                longitud.setText(String.valueOf(ubicacion.getLongitude()));
                latitud.setError(null); longitud.setError(null);
                estadoUbicacion.setText(R.string.ubicacion_obtenida);
                boolean abrir = abrirMapaDespues; abrirMapaDespues = false;
                if (abrir) abrirMapa();
            });
        } catch (SecurityException | IllegalArgumentException e) {
            cancelarUbicacion(); abrirMapaDespues = false;
            estadoUbicacion.setText(R.string.ubicacion_no_disponible); aviso(R.string.ubicacion_no_disponible);
        }
    }

    private void cancelarUbicacion() {
        handler.removeCallbacks(timeoutUbicacion);
        if (solicitudUbicacion != null) { solicitudUbicacion.cancel(); solicitudUbicacion = null; }
        if (btnUbicacion != null) btnUbicacion.setEnabled(true);
    }

    private void actualizarFoto() {
        btnVerFoto.setEnabled(fotoUri != null);
        estadoFoto.setText(fotoUri == null ? R.string.foto_sin_captura : R.string.foto_guardada);
    }
    private static String texto(EditText campo) { return campo.getText().toString().trim(); }
    private static Uri uriGuardada(Bundle estado, String clave) {
        String valor = estado.getString(clave); return valor == null ? null : Uri.parse(valor);
    }
    private void aviso(int mensaje) { Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show(); }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);
        if (fotoUri != null) estado.putString("foto_uri", fotoUri.toString());
        if (fotoPendiente != null) estado.putString("foto_pendiente", fotoPendiente.toString());
        estado.putBoolean("abrir_mapa", abrirMapaDespues);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (animacionTienda != null) animacionTienda.stop();
        if (solicitudUbicacion != null) {
            cancelarUbicacion(); abrirMapaDespues = false;
            estadoUbicacion.setText(R.string.ubicacion_interrumpida);
        }
    }
}
