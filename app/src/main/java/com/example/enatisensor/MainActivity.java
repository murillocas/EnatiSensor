package com.example.enatisensor;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.sql.Time;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "APP";

    private SensorManager sensorManager;
    private Sensor aceleSesor;

    private TextView textdirecoes,textLinear,textMedia,text2,txtTempo;
    private TextView aresta1,aresta2,aresta3,aresta4,txtcont;

    private Button btngravar1,btnparar;
    private FloatingActionButton btnComecarlista;
    private Handler handler = new Handler();
    int arestascont = 1;

    private float prevX = 0,prevY=0,prevz=0;
    private float nowX,nowY,nowZ;

    int tempoPercorido = 0;
    boolean contarTempo = false;
    boolean listar = false;
    float media = 0;


    float mediaaceleracoes =0;
    ArrayList<Float> aceleracoes = new ArrayList<Float>();
    ArrayList<Float> arestasComodo = new ArrayList<Float>();

    Calendar calendar = Calendar.getInstance();
    int MinutosAntes ;
    int MinutosDepois;
    int segundosAntes;
    int segundosDepois;
    float milisegundoAntes;
    float MilisegundoDepois;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btngravar1 = findViewById(R.id.btngravar);
        btnparar = findViewById(R.id.btnfinalcontagem);
        btnComecarlista = findViewById(R.id.btnComecarlista);

        textdirecoes = findViewById(R.id.txtdirecao);
        textLinear = findViewById(R.id.txtLinear);
        textMedia = findViewById(R.id.txtmedia);
        text2 = findViewById(R.id.text2);
        txtTempo = findViewById(R.id.txtTempo);

        aresta1 = findViewById(R.id.aresta1);
        aresta2 = findViewById(R.id.aresta2);
        aresta3 = findViewById(R.id.aresta3);
        aresta4 = findViewById(R.id.aresta4);
        txtcont = findViewById(R.id.txtcont);

        calendar.setTimeInMillis(System.currentTimeMillis());
        MinutosAntes = calendar.get(Calendar.MINUTE);
        segundosAntes = calendar.get(Calendar.SECOND);

        findSensors();
        selectSensors();
        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);

        btngravar1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: clicado");
                contarTempo = true;
                if(contarTempo){
                    Log.i(TAG, "onClick: inicio contagem");
                    media = 0;
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    LocalDateTime agora = LocalDateTime.now();
                    MinutosAntes = Integer.parseInt(DateTimeFormatter.ofPattern("mm").format(agora)) ;
                    segundosAntes = Integer.parseInt(DateTimeFormatter.ofPattern("ss").format(agora)) ;
                    milisegundoAntes = Float.parseFloat(DateTimeFormatter.ofPattern("SS").format(agora))/100;


                    //tempopercorido();
                }

            }
        });
        btnComecarlista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (listar){
                    while (arestasComodo.size() >0){
                        for (int x =0 ; x<4;x++){
                            if (x==0){
                              aresta1.setText(""+arestasComodo.remove(0));
                            }
                            if (x==0){
                               aresta2.setText(""+arestasComodo.remove(0));
                            }
                            if (x==0){
                                aresta3.setText("" + arestasComodo.remove(0));
                            }
                            if (x==0){
                                aresta4.setText("" +arestasComodo.remove(0));
                            }
                        }
                        arestasComodo.clear();

                    }
                    listar = false;
                }else{
                    listar = true;
                    arestascont = 1;
                }

            }
        });
        btnparar.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                contarTempo = false;
                tempoPercorido -=1;

                DecimalFormat digitos = new DecimalFormat(" 0.00");
                LocalDateTime agora = LocalDateTime.now();
                MinutosDepois = Integer.parseInt(DateTimeFormatter.ofPattern("mm").format(agora)) ;
                segundosDepois = Integer.parseInt(DateTimeFormatter.ofPattern("ss").format(agora)) ;
                MilisegundoDepois = Float.parseFloat(DateTimeFormatter.ofPattern("SS").format(agora))/100;
                fazerMediaAceleracoes();

                Log.i(TAG, "onClick: tempos depois : minutos " +MinutosDepois +" segundos " + segundosDepois + " miliseconds "+ MilisegundoDepois + "\n antes : minutos " + MinutosAntes +" segundos "+ segundosAntes +"milisegundos" + milisegundoAntes);
                float terceiravaiaveauxtempo = (MinutosDepois * 60) + segundosDepois + MilisegundoDepois - (MinutosAntes * 60) - segundosAntes - milisegundoAntes;

                Log.i(TAG, "onClick: terceiravaiaveauxtempo" + terceiravaiaveauxtempo);
                Log.i(TAG, "onClick: mediadevelocidade" + (mediaaceleracoes*terceiravaiaveauxtempo));
/*
                Log.i(TAG, "onClick: teste distancia percorida " +((mediaaceleracoes * terceiravaiaveauxtempo * terceiravaiaveauxtempo )/2));
                Log.i(TAG, "onClick: teste distancia percorida2  " +(mediaaceleracoes * terceiravaiaveauxtempo )/2);
                Log.i(TAG, "onClick: teste distancia percorida3 " + Math.sqrt(mediaaceleracoes *mediaaceleracoes) );
                Log.i(TAG, "onClick: teste distancia percorida4 " + Math.sqrt((mediaaceleracoes * terceiravaiaveauxtempo )) );
                Log.i(TAG, "onClick: teste distancia percorida5 " + Math.sqrt((mediaaceleracoes * terceiravaiaveauxtempo )/2 * (mediaaceleracoes * terceiravaiaveauxtempo )/2) );*/
                Log.i(TAG, "onClick: teste distancia percorida6 " + Math.sqrt((mediaaceleracoes  * (terceiravaiaveauxtempo * terceiravaiaveauxtempo ))/2)) ;
                textMedia.setText(""+ mediaaceleracoes);
//                text2.setText(""+(mediaaceleracoes*terceiravaiaveauxtempo));
                float aresta = (float) Math.sqrt((mediaaceleracoes  * (terceiravaiaveauxtempo * terceiravaiaveauxtempo ))/2);
                text2.setText(""+ aresta );
                txtTempo.setText("" + terceiravaiaveauxtempo);
                if (listar){
                    arestasComodo.add(aresta);
                    txtcont.setText(""+arestascont);
                    arestascont++;
                }

                media =0;
                tempoPercorido =0;
                limparvetor();
            }
        });
    }

    public void limparvetor(){
        while (aceleracoes.size() >0){
            aceleracoes.remove(0);
        }
        aceleracoes.clear();
        Log.i(TAG, "limparvetor: size final" + aceleracoes.size());
    }
    public void tempopercorido(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//             Log.i(TAG, "run: testes");
                if(contarTempo){
                    tempoPercorido += 1;
                    tempopercorido();

                }

            }
        },1000);
    }







    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, aceleSesor, SensorManager.SENSOR_DELAY_NORMAL);


    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener( this);

    }

    /*verificando se exite um sensor em especifico caso nao selecionando um DEFAULT*/
    public void selectSensors(){

        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        aceleSesor = null;
        aceleSesor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if(aceleSesor == null){
            Log.d(TAG, "selectSensors: Sensor nao encontrado");
        }else{
            Log.d(TAG, "selectSensors: Sensor encontrado" + aceleSesor);
        }



    }

    /*Listar todos os sensores do dispositivo*/
    public void findSensors(){
        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (int i = 0 ; i < deviceSensors.size() ; i++){
            Log.i(TAG, "onCreate: lista de sensores " + deviceSensors.get(i));
        }
    }



    public void fazerMediaAceleracoes(){
        for (int x=0 ;x< aceleracoes.size()-1;x++){
            mediaaceleracoes += aceleracoes.get(x);
        }
        mediaaceleracoes = mediaaceleracoes / aceleracoes.size();
        Log.i(TAG, "fazerMediaAceleracoes: a media de acelleracoes e " + mediaaceleracoes);
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        DecimalFormat digitos = new DecimalFormat(" 0.00");

        nowX = Math.round(sensorEvent.values[0]);
        nowY = Math.round(sensorEvent.values[1]);
        nowZ = Math.round(sensorEvent.values[2]);

        //digitos.format(sensorEvent.values[0]);
        if(contarTempo){
            aceleracoes.add((float) Math.sqrt( nowX*nowX + nowY*nowY + nowZ*nowZ ));
            /*if(media ==0){
                media = (float) Math.sqrt( nowX*nowX + nowY*nowY + nowZ*nowZ );
            }else{
                media = (float) ((Math.sqrt( nowX*nowX + nowY*nowY + nowZ*nowZ ) + media )/ 2);
            }*/
        }

//        Log.i(TAG, "LINEAR " + sensorEvent.values[0] + sensorEvent.values[1] +sensorEvent.values[2]);
        textdirecoes.setText("values x " + nowX +" y "+ nowY +" z "+ nowZ);
        textLinear.setText("" + Math.sqrt( nowX*nowX + nowY*nowY + nowZ*nowZ ));





    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}