/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zgas.supervisor.IA;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.camera2.CameraCharacteristics;
import android.media.ImageReader.OnImageAvailableListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import zgas.supervisor.Clase;
import zgas.supervisor.Home;
import zgas.supervisor.IA.customview.OverlayView;
import zgas.supervisor.IA.customview.OverlayView.DrawCallback;
import zgas.supervisor.IA.env.BorderedText;
import zgas.supervisor.IA.env.ImageUtils;
import zgas.supervisor.IA.env.Logger;
import zgas.supervisor.IA.tflite.SimilarityClassifier;
import zgas.supervisor.IA.tflite.TFLiteObjectDetectionAPIModel;
import zgas.supervisor.IA.tracking.MultiBoxTracker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import zgas.supervisor.MainActivity;
import zgas.supervisor.R;
import zgas.supervisor.models.Client;
import zgas.supervisor.models.IAData;
import zgas.supervisor.providers.RegistroProvider;

import static zgas.supervisor.HomeRegistrarOperador.checkedFoto;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();


  // FaceNet
//  private static final int TF_OD_API_INPUT_SIZE = 160;
//  private static final boolean TF_OD_API_IS_QUANTIZED = false;
//  private static final String TF_OD_API_MODEL_FILE = "facenet.tflite";
//  //private static final String TF_OD_API_MODEL_FILE = "facenet_hiroki.tflite";

  // MobileFaceNet
  private static final int TF_OD_API_INPUT_SIZE = 112;
  private static final boolean TF_OD_API_IS_QUANTIZED = false;
  private static final String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";


  private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";

  private static final DetectorMode MODE = DetectorMode.TF_OD_API;
  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
  private static final boolean MAINTAIN_ASPECT = false;

  private static final Size DESIRED_PREVIEW_SIZE = new Size(1280, 960);
  //private static final int CROP_SIZE = 320;
  //private static final Size CROP_SIZE = new Size(320, 320);


  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  private SimilarityClassifier detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;
  private boolean addPending = false;
  //private boolean adding = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;
  //private Matrix cropToPortraitTransform;

  private MultiBoxTracker tracker;

  private BorderedText borderedText;

  // Face detector
  private FaceDetector faceDetector;

  // here the preview image is drawn in portrait way
  private Bitmap portraitBmp = null;
  // here the face is cropped and drawn
  private Bitmap faceBmp = null;

  private FloatingActionButton fabAdd;

  //private HashMap<String, Classifier.Recognition> knownFaces = new HashMap<>();
  String numNomina;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    numNomina = getIntent().getStringExtra("numNomina");



    fabAdd = findViewById(R.id.fab_add);
    fabAdd.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onAddClick();
      }
    });

    // Real-time contour detection of multiple faces
    FaceDetectorOptions options =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                    .build();


    FaceDetector detector = FaceDetection.getClient(options);

    faceDetector = detector;


    //checkWritePermission();





    //cargarDatosFB("73539");



  }



  private void onAddClick() {

    addPending = true;
    //Toast.makeText(this, "click", Toast.LENGTH_LONG ).show();

  }

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);


    try {
      detector =
              TFLiteObjectDetectionAPIModel.create(
                      getAssets(),
                      TF_OD_API_MODEL_FILE,
                      TF_OD_API_LABELS_FILE,
                      TF_OD_API_INPUT_SIZE,
                      TF_OD_API_IS_QUANTIZED);
      //cropSize = TF_OD_API_INPUT_SIZE;
    } catch (final IOException e) {
      e.printStackTrace();
      LOGGER.e(e, "Exception initializing classifier!");
      Toast toast =
              Toast.makeText(
                      getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);


    int targetW, targetH;
    if (sensorOrientation == 90 || sensorOrientation == 270) {
      targetH = previewWidth;
      targetW = previewHeight;
    }
    else {
      targetW = previewWidth;
      targetH = previewHeight;
    }
    int cropW = (int) (targetW / 2.0);
    int cropH = (int) (targetH / 2.0);

    croppedBitmap = Bitmap.createBitmap(cropW, cropH, Config.ARGB_8888);

    portraitBmp = Bitmap.createBitmap(targetW, targetH, Config.ARGB_8888);
    faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Config.ARGB_8888);

    frameToCropTransform =
            ImageUtils.getTransformationMatrix(
                    previewWidth, previewHeight,
                    cropW, cropH,
                    sensorOrientation, MAINTAIN_ASPECT);

//    frameToCropTransform =
//            ImageUtils.getTransformationMatrix(
//                    previewWidth, previewHeight,
//                    previewWidth, previewHeight,
//                    sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);


    Matrix frameToPortraitTransform =
            ImageUtils.getTransformationMatrix(
                    previewWidth, previewHeight,
                    targetW, targetH,
                    sensorOrientation, MAINTAIN_ASPECT);



    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
            new DrawCallback() {
              @Override
              public void drawCallback(final Canvas canvas) {
                tracker.draw(canvas);
                if (isDebug()) {
                  tracker.drawDebug(canvas);
                }
              }
            });

    tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
  }


  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;

    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }

    InputImage image = InputImage.fromBitmap(croppedBitmap, 0);
    faceDetector
            .process(image)
            .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
              @Override
              public void onSuccess(List<Face> faces) {
                if (faces.size() == 0) {
                  updateResults(currTimestamp, new LinkedList<>());
                  return;
                }
                runInBackground(
                        new Runnable() {
                          @Override
                          public void run() {
                            onFacesDetected(currTimestamp, faces, addPending);
                            addPending = false;
                          }
                        });
              }

            });


  }

  @Override
  protected int getLayoutId() {
    return R.layout.tfe_od_camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.
  private enum DetectorMode {
    TF_OD_API;
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    runInBackground(() -> detector.setUseNNAPI(isChecked));
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    runInBackground(() -> detector.setNumThreads(numThreads));
  }


  // Face Processing
  private Matrix createTransform(
          final int srcWidth,
          final int srcHeight,
          final int dstWidth,
          final int dstHeight,
          final int applyRotation) {

    Matrix matrix = new Matrix();
    if (applyRotation != 0) {
      if (applyRotation % 90 != 0) {
        LOGGER.w("Rotation of %d % 90 != 0", applyRotation);
      }

      // Translate so center of image is at origin.
      matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

      // Rotate around origin.
      matrix.postRotate(applyRotation);
    }

//        // Account for the already applied rotation, if any, and then determine how
//        // much scaling is needed for each axis.
//        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;
//        final int inWidth = transpose ? srcHeight : srcWidth;
//        final int inHeight = transpose ? srcWidth : srcHeight;

    if (applyRotation != 0) {

      // Translate back from origin centered reference to destination frame.
      matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
    }

    return matrix;

  }


  boolean pressButton = false;
  private void showAddFaceDialog(SimilarityClassifier.Recognition rec) {

    Dialog popupEliminarDireccion;


    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    LayoutInflater inflater = getLayoutInflater();
    View dialogLayout = inflater.inflate(R.layout.image_edit_dialog, null);
    ImageView ivFace = dialogLayout.findViewById(R.id.dlg_image);
    TextView tvTitle = dialogLayout.findViewById(R.id.dlg_title);
    EditText etName = dialogLayout.findViewById(R.id.dlg_input);

    tvTitle.setText("Add Face");
    ivFace.setImageBitmap(rec.getCrop());
    etName.setHint(numNomina);

    Button btnConfirmar = dialogLayout.findViewById(R.id.btnConfirmar);


    btnConfirmar.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {


        if(pressButton)
          return;
        else {


          Toast.makeText(DetectorActivity.this, "Subiendo foto, espere un momento.", Toast.LENGTH_SHORT).show();
          pressButton = true;
          String name = numNomina;
          if (name.isEmpty()) {
            return;
          }
          guardarDatosFB(numNomina, rec);
        }


        //knownFaces.put(name, rec);


      }
    });




    /*
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dlg, int i) {


      }
    });

    */



    try {
      builder.setView(dialogLayout);
      builder.show();
    }
    catch (Exception e)
    {

    }




    }








  IAData iaData = new IAData();
  private void cargarDatosFB(String numNomina) {


    RegistroProvider registroProvider = new RegistroProvider();
    registroProvider.getIAt(numNomina).addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (snapshot.exists()) {
          try {
            iaData.setId(Objects.requireNonNull(snapshot.child("id").getValue()).toString());
            iaData.setTitle(Objects.requireNonNull(snapshot.child("title").getValue()).toString());
            iaData.setDistance(Float.parseFloat(Objects.requireNonNull(snapshot.child("distance").getValue()).toString()));
            iaData.setLeft(Float.parseFloat(Objects.requireNonNull(snapshot.child("left").getValue()).toString()));
            iaData.setTop(Float.parseFloat(Objects.requireNonNull(snapshot.child("top").getValue()).toString()));
            iaData.setRight(Float.parseFloat(Objects.requireNonNull(snapshot.child("right").getValue()).toString()));
            iaData.setBottom(Float.parseFloat(Objects.requireNonNull(snapshot.child("bottom").getValue()).toString()));
            iaData.setColor(Integer.parseInt(snapshot.child("color").getValue().toString()));

            RectF location = new RectF(iaData.getLeft(), iaData.getTop(), iaData.getRight(), iaData.getBottom());

            Log.d("MAIN", "ID: " + iaData.getId() + "\n" +
                    "Title: " + iaData.getTitle() + "\n" +
                    "Distancia: " + iaData.getDistance() + "\n" +
                    "Left: " + iaData.getLeft() + "\n" +
                    "Top: " + iaData.getTop() + "\n" +
                    "Right: " + iaData.getRight() + "\n" +
                    "Bottom: " + iaData.getBottom() + "\n"+
                    "color: " + iaData.getColor() + "\n"
            );
            //Toast.makeText(DetectorActivity.this, "Carga de datos correcta.", Toast.LENGTH_SHORT).show();

            descargarObject("73539");

          }
          catch (Exception e)
          {
            Toast.makeText(DetectorActivity.this, "Error Catch." +e.getMessage(), Toast.LENGTH_SHORT).show();
          }
        }
        else
          Toast.makeText(DetectorActivity.this, "NO Existen Datos.", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Toast.makeText(DetectorActivity.this, "ERROR DE CARGA DE DATOS.", Toast.LENGTH_SHORT).show();
      }
    });





  }

  Object recuperado;
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


      File f = new File(Environment.getExternalStorageDirectory()+"/IA", numNomina+".data");
      ObjectInputStream recuperando = new ObjectInputStream(new FileInputStream(f));
      recuperado = (Object) recuperando.readObject();
      recuperando.close();




      descargarBitmap("73539");


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



          recDownload = new SimilarityClassifier.Recognition(iaData.getId(), iaData.getTitle(), iaData.getDistance(),new RectF(iaData.getLeft(), iaData.getTop(), iaData.getRight(), iaData.getBottom()));
          recDownload.setCrop(bitmap);
          recDownload.setExtra(recuperado);
          recDownload.setColor(iaData.getColor());

          try {
            detector.register("JESUSIN", recDownload);
          }
          catch (Exception e)
          {
            Toast.makeText(DetectorActivity.this, "Errror" + e.getMessage(), Toast.LENGTH_SHORT).show();
          }


          //imageView.setImageBitmap(bitmap);


          Toast.makeText(DetectorActivity.this, "Carga Completa", Toast.LENGTH_SHORT).show();

        }
      }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception exception) {
        }
      });
    } catch (Exception e ) {}
  }


  SimilarityClassifier.Recognition recDownload;


  private void guardarObjectCS(String numNomina, SimilarityClassifier.Recognition rec) {
    //GENERAR CARPETA IA
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

    ////
    try {

      File fichero = new File(Environment.getExternalStorageDirectory()+"/IA", "IA.data");

      ObjectOutputStream fpsalida = new ObjectOutputStream(new FileOutputStream(fichero));
      fpsalida.writeObject(rec.getExtra());
      fpsalida.close();
    }
    catch (Exception e) {}


    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://zgas-proyecto.appspot.com").child(numNomina + ".data");

    UploadTask uploadTask;

    Uri file = Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/IA", "IA.data"));
    uploadTask = storageRef.putFile(file);




// Register observers to listen for when the download is done or if it fails
    uploadTask.addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception exception) {
        // Handle unsuccessful uploads
      }
    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
      @Override
      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
        // ...
        Toast.makeText(DetectorActivity.this, "Object Subido Correctamente", Toast.LENGTH_SHORT).show();





        checkedFoto = true;
        finish();

      }
    });
  }

  private void guardarFotoCS(String numNomina, Bitmap crop, SimilarityClassifier.Recognition rec) {


    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://zgas-proyecto.appspot.com").child(numNomina + ".jpg");

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    crop.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
    byte[] data = outputStream.toByteArray();


    UploadTask uploadTask = storageRef.putBytes(data);
    uploadTask.addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception exception) {
      }
    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
      @Override
      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        guardarObjectCS(numNomina, rec);
      }
    });

  }

  private void guardarDatosFB(String numNomina, SimilarityClassifier.Recognition rec) {

    RegistroProvider registroProvider = new RegistroProvider();
    IAData iaData = new IAData(rec.getId(), rec.getTitle(), rec.getDistance(), rec.getLocation().left, rec.getLocation().top, rec.getLocation().right, rec.getLocation().bottom, rec.getColor());

    registroProvider.createIA(numNomina, iaData).addOnCompleteListener(new OnCompleteListener<Void>() {
      @Override
      public void onComplete(@NonNull Task<Void> task) {
        guardarFotoCS(numNomina, rec.getCrop(), rec);
      }
    });

  }





  public boolean moveFile(String fromFile, String toFile) {
    //File origin = new File(Environment.getExternalStorageDirectory()+"/miAppFelipe", fromFile);

    File origin = new File(fromFile);
    File destination = new File(Environment.getExternalStorageDirectory()+"/IA", toFile);
    if (origin.exists()) {
      try {
        InputStream in = new FileInputStream(origin);
        OutputStream out = new FileOutputStream(destination);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
        in.close();
        out.close();
        return origin.delete();
      } catch (IOException ioe) {
        ioe.printStackTrace();
        return false;
      }
    } else {
      return false;
    }
  }

  public boolean copyFile(String fromFile, String toFile) {
    File origin = new File(fromFile);
    File destination = new File(toFile);
    if (origin.exists()) {
      try {
        InputStream in = new FileInputStream(origin);
        OutputStream out = new FileOutputStream(destination);
        // We use a buffer for the copy (Usamos un buffer para la copia).
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
        in.close();
        out.close();
        return true;
      } catch (IOException ioe) {
        ioe.printStackTrace();
        return false;
      }
    } else {
      return false;
    }
  }

  private void updateResults(long currTimestamp, final List<SimilarityClassifier.Recognition> mappedRecognitions) {

    tracker.trackResults(mappedRecognitions, currTimestamp);
    trackingOverlay.postInvalidate();
    computingDetection = false;
    //adding = false;


    if (mappedRecognitions.size() > 0) {
       LOGGER.i("Adding results");
       SimilarityClassifier.Recognition rec = mappedRecognitions.get(0);
       if (rec.getExtra() != null) {
         showAddFaceDialog(rec);
       }

    }

    runOnUiThread(
            new Runnable() {
              @Override
              public void run() {
                showFrameInfo(previewWidth + "x" + previewHeight);
                showCropInfo(croppedBitmap.getWidth() + "x" + croppedBitmap.getHeight());
                showInference(lastProcessingTimeMs + "ms");
              }
            });

  }

  private void onFacesDetected(long currTimestamp, List<Face> faces, boolean add) {

    cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
    final Canvas canvas = new Canvas(cropCopyBitmap);
    final Paint paint = new Paint();
    paint.setColor(Color.RED);
    paint.setStyle(Style.STROKE);
    paint.setStrokeWidth(2.0f);

    float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
    switch (MODE) {
      case TF_OD_API:
        minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
        break;
    }

    final List<SimilarityClassifier.Recognition> mappedRecognitions =
            new LinkedList<SimilarityClassifier.Recognition>();


    //final List<Classifier.Recognition> results = new ArrayList<>();

    // Note this can be done only once
    int sourceW = rgbFrameBitmap.getWidth();
    int sourceH = rgbFrameBitmap.getHeight();
    int targetW = portraitBmp.getWidth();
    int targetH = portraitBmp.getHeight();
    Matrix transform = createTransform(
            sourceW,
            sourceH,
            targetW,
            targetH,
            sensorOrientation);
    final Canvas cv = new Canvas(portraitBmp);

    // draws the original image in portrait mode.
    cv.drawBitmap(rgbFrameBitmap, transform, null);

    final Canvas cvFace = new Canvas(faceBmp);

    boolean saved = false;

    for (Face face : faces) {

      LOGGER.i("FACE" + face.toString());
      LOGGER.i("Running detection on face " + currTimestamp);
      //results = detector.recognizeImage(croppedBitmap);

      final RectF boundingBox = new RectF(face.getBoundingBox());

      //final boolean goodConfidence = result.getConfidence() >= minimumConfidence;
      final boolean goodConfidence = true; //face.get;
      if (boundingBox != null && goodConfidence) {

        // maps crop coordinates to original
        cropToFrameTransform.mapRect(boundingBox);

        // maps original coordinates to portrait coordinates
        RectF faceBB = new RectF(boundingBox);
        transform.mapRect(faceBB);

        // translates portrait to origin and scales to fit input inference size
        //cv.drawRect(faceBB, paint);
        float sx = ((float) TF_OD_API_INPUT_SIZE) / faceBB.width();
        float sy = ((float) TF_OD_API_INPUT_SIZE) / faceBB.height();
        Matrix matrix = new Matrix();
        matrix.postTranslate(-faceBB.left, -faceBB.top);
        matrix.postScale(sx, sy);

        cvFace.drawBitmap(portraitBmp, matrix, null);

        //canvas.drawRect(faceBB, paint);

        String label = "";
        float confidence = -1f;
        Integer color = Color.BLUE;
        Object extra = null;
        Bitmap crop = null;

        if (add) {
          crop = Bitmap.createBitmap(portraitBmp,
                            (int) faceBB.left,
                            (int) faceBB.top,
                            (int) faceBB.width(),
                            (int) faceBB.height());
        }

        final long startTime = SystemClock.uptimeMillis();
        final List<SimilarityClassifier.Recognition> resultsAux = detector.recognizeImage(faceBmp, add);
        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

        if (resultsAux.size() > 0) {

          SimilarityClassifier.Recognition result = resultsAux.get(0);

          extra = result.getExtra();
//          Object extra = result.getExtra();
//          if (extra != null) {
//            LOGGER.i("embeeding retrieved " + extra.toString());
//          }

          float conf = result.getDistance();
          if (conf < 1.0f) {

            confidence = conf;
            label = result.getTitle();
            if (result.getId().equals("0")) {
              color = Color.GREEN;
            }
            else {
              color = Color.RED;
            }
          }

        }

        if (getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT) {

          // camera is frontal so the image is flipped horizontally
          // flips horizontally
          Matrix flip = new Matrix();
          if (sensorOrientation == 90 || sensorOrientation == 270) {
            flip.postScale(1, -1, previewWidth / 2.0f, previewHeight / 2.0f);
          }
          else {
            flip.postScale(-1, 1, previewWidth / 2.0f, previewHeight / 2.0f);
          }
          //flip.postScale(1, -1, targetW / 2.0f, targetH / 2.0f);
          flip.mapRect(boundingBox);

        }

        final SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                "0", label, confidence, boundingBox);

        result.setColor(color);
        result.setLocation(boundingBox);
        result.setExtra(extra);
        result.setCrop(crop);
        mappedRecognitions.add(result);

      }


    }

    //    if (saved) {
//      lastSaved = System.currentTimeMillis();
//    }

    updateResults(currTimestamp, mappedRecognitions);


  }


}
