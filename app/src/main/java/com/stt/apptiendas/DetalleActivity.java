package com.stt.apptiendas;

import android.content.Intent; // Agregar este import
import android.os.Bundle;
import android.widget.Toast; // Agregar este import

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DetalleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Recuperamos el Intent y aplicamos validación para evitar nulls[cite: 6]
        Intent intent = getIntent();

        if (intent != null) {
            // 2. Extraemos los datos enviados
            String nombre = intent.getStringExtra("NOMBRE_TIENDA");
            String web = intent.getStringExtra("WEB_TIENDA");

            // Validación adicional: si los datos no llegaron, asignamos un valor por defecto[cite: 6]
            if (nombre == null) nombre = "Tienda sin nombre";
            if (web == null) web = "Sin web";

            // Para verificar que funciona, mostraremos un mensaje temporal (Toast).
            // Más adelante, aquí usarás un TextView para mostrarlo en pantalla (ej: tvNombre.setText(nombre);)
            Toast.makeText(this, "Datos cargados: " + nombre, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "No se recibieron datos", Toast.LENGTH_SHORT).show();
        }
    }
}