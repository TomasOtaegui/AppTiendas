package com.stt.apptiendas;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhotoActivity extends AppCompatActivity {
    private final ExecutorService lector = Executors.newSingleThreadExecutor();
    @Override
    protected void onCreate(Bundle estadoGuardado) {
        super.onCreate(estadoGuardado);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_photo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
        findViewById(R.id.btn_volver_foto).setOnClickListener(v -> finish());
        ImageView imagen = findViewById(R.id.iv_foto);
        TextView estado = findViewById(R.id.tv_estado_imagen);
        String uri = getIntent().getStringExtra("FOTO_URI");
        if (uri == null || uri.trim().isEmpty()) { estado.setText(R.string.foto_primero); return; }
        // La imagen se reduce para la vista; el archivo de la galería conserva su resolución.
        lector.execute(() -> {
            Bitmap bitmap = null;
            try {
                Uri foto = Uri.parse(uri);
                if (!"content".equals(foto.getScheme())) throw new IllegalArgumentException("URI inválida");
                BitmapFactory.Options opciones = new BitmapFactory.Options();
                opciones.inJustDecodeBounds = true;
                try (InputStream entrada = getContentResolver().openInputStream(foto)) {
                    BitmapFactory.decodeStream(entrada, null, opciones);
                }
                if (opciones.outWidth <= 0 || opciones.outHeight <= 0) throw new IOException("Imagen inválida");
                opciones.inSampleSize = 1;
                while (Math.max(opciones.outWidth, opciones.outHeight) / opciones.inSampleSize > 1600) opciones.inSampleSize *= 2;
                opciones.inJustDecodeBounds = false;
                try (InputStream entrada = getContentResolver().openInputStream(foto)) {
                    bitmap = BitmapFactory.decodeStream(entrada, null, opciones);
                }
                if (bitmap != null) {
                    try (InputStream entrada = getContentResolver().openInputStream(foto)) {
                        ExifInterface exif = new ExifInterface(entrada);
                        int orientacion = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL);
                        Matrix giro = new Matrix();
                        switch (orientacion) {
                            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: giro.setScale(-1, 1); break;
                            case ExifInterface.ORIENTATION_ROTATE_180: giro.setRotate(180); break;
                            case ExifInterface.ORIENTATION_FLIP_VERTICAL: giro.setScale(1, -1); break;
                            case ExifInterface.ORIENTATION_TRANSPOSE: giro.setRotate(90); giro.postScale(-1, 1); break;
                            case ExifInterface.ORIENTATION_ROTATE_90: giro.setRotate(90); break;
                            case ExifInterface.ORIENTATION_TRANSVERSE: giro.setRotate(270); giro.postScale(-1, 1); break;
                            case ExifInterface.ORIENTATION_ROTATE_270: giro.setRotate(270); break;
                            default: break;
                        }
                        if (!giro.isIdentity()) {
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                    bitmap.getWidth(), bitmap.getHeight(), giro, true);
                        }
                    } catch (IOException ignored) { /* Se conserva la imagen si no tiene metadatos EXIF. */ }
                }
            } catch (IOException | SecurityException | IllegalArgumentException e) { /* Se muestra un error recuperable. */ }
            Bitmap resultado = bitmap;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (resultado == null) estado.setText(R.string.foto_no_accesible);
                else { imagen.setImageBitmap(resultado); estado.setText(R.string.foto_lista); }
            });
        });
    }
    @Override
    protected void onDestroy() { lector.shutdownNow(); super.onDestroy(); }
}
