package zgas.supervisor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class descargarIMG extends AppCompatActivity {

    Button button;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descargar_i_m_g);

        button = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView2);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                descargarBitmap("73539");
                descargarObject("73539");
            }
        });
    }

    private void descargarObject(String numNomina) {
        try {

            ///GENERAR ARCHIVO
            File carpeta = new File(Environment.getExternalStorageDirectory()+"/IA");

            //comprobar si la carpeta no existe, entonces crearla
            if(!carpeta.exists()) {
                //carpeta.mkdir() creará la carpeta en la ruta indicada al inicializar el objeto File
                if(carpeta.mkdir())
                    Toast.makeText(getApplicationContext(), "Carpeta creada : " + carpeta.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                //se ha creado la carpeta;
            }else
            {
                //la carpeta ya existe
                Toast.makeText(getApplicationContext(), "Carpeta existente : " + carpeta.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://zgas-proyecto.appspot.com").child(numNomina + ".data");
            File destination = new File(Environment.getExternalStorageDirectory()+"/IA", numNomina + ".data");

            //final File localFile = File.createTempFile(numNomina, "data", destination);
            storageRef.getFile(destination).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
            Toast.makeText(this, "Carga Completa", Toast.LENGTH_SHORT).show();
        } catch (Exception e ) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void descargarBitmap(String numNomina) {
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://zgas-proyecto.appspot.com").child(numNomina + ".jpg");

            File destination = new File(Environment.getExternalStorageDirectory()+"/IA", numNomina + ".jpg");
            storageRef.getFile(destination).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(destination.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        } catch (Exception e ) {}
    }


    public static Bitmap decodeUriToBitmap(Context mContext, Uri sendUri) {
        Bitmap getBitmap = null;
        try {
            InputStream image_stream;
            try {
                image_stream = mContext.getContentResolver().openInputStream(sendUri);
                getBitmap = BitmapFactory.decodeStream(image_stream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getBitmap;
    }


}