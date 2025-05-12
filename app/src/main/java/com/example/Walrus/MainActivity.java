package com.example.Walrus;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button buttonStartStop;

    private Button buttonSOS;
    private Button buttonHello;
    private Button buttonYes;
    private Button buttonNo;
    private Button buttonGood;
    private Button buttonBad;
    private Button buttonFriend;
    private Button buttonEnemy;
    private Button buttonCome;
    private Button buttonFlee;

    private MaterialSwitch switchFlash;
    private MaterialSwitch switchFlashBip;
    private MaterialSwitch switchBip;


    private static final int CAMERA_REQUEST = 50;

    private boolean boucleActive = false;
    private boolean permissionOK = false;
    private EditText edtTexteATraduire;
    private String texteATraduire;

    // Création du dictionnaire pour le code Morse
    private Map<String, String> morseCodeMap = new HashMap<>();
    private Handler handler = new Handler();
    private Runnable runnable;
    private int pointeurSequence = 0;
    private int vitessePoint = 500;

    private Slider sliderVitesse;

    private ToneGenerator toneG;

    private boolean onlyFlash;
    private boolean onlyBip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Récupération des boutons et autres éléments de l'IHM
        // --- boutons d'actions prédéfinies
        buttonStartStop = findViewById(R.id.bouton_lampe_torche);
        buttonSOS = findViewById(R.id.buttonSos);
        buttonHello = findViewById(R.id.buttonHello);
        buttonYes = findViewById(R.id.buttonYes);
        buttonNo = findViewById(R.id.buttonNo);
        buttonGood = findViewById(R.id.buttonGood);
        buttonBad = findViewById(R.id.buttonBad);
        buttonFriend = findViewById(R.id.buttonFriend);
        buttonEnemy = findViewById(R.id.buttonEnemy);
        buttonCome = findViewById(R.id.buttonCome);
        buttonFlee = findViewById(R.id.buttonFlee);

        switchFlash = findViewById(R.id.switchFlash);
        switchBip = findViewById(R.id.switchBip);

        // --- editText du texte à traduire
        edtTexteATraduire = findViewById(R.id.edt_TexteATraduire);

        // --- slider de la vitesse du flash (vitesse d'un point)
        sliderVitesse = findViewById(R.id.sliderVitesse);

        switchFlash.setChecked(true);
        onlyFlash = true;
        onlyBip = false;

        // Remplissage du dictionnaire avec les symboles et leur représentation en Morse
        morseCodeMap.put("A", "01");
        morseCodeMap.put("B", "1000");
        morseCodeMap.put("C", "1010");
        morseCodeMap.put("D", "100");
        morseCodeMap.put("E", "0");
        morseCodeMap.put("F", "0010");
        morseCodeMap.put("G", "110");
        morseCodeMap.put("H", "0000");
        morseCodeMap.put("I", "00");
        morseCodeMap.put("J", "0111");
        morseCodeMap.put("K", "101");
        morseCodeMap.put("L", "0100");
        morseCodeMap.put("M", "11");
        morseCodeMap.put("N", "10");
        morseCodeMap.put("O", "111");
        morseCodeMap.put("P", "0110");
        morseCodeMap.put("Q", "1101");
        morseCodeMap.put("R", "010");
        morseCodeMap.put("S", "000");
        morseCodeMap.put("T", "1");
        morseCodeMap.put("U", "001");
        morseCodeMap.put("V", "0001");
        morseCodeMap.put("W", "011");
        morseCodeMap.put("X", "1001");
        morseCodeMap.put("Y", "1011");
        morseCodeMap.put("Z", "1100");

        morseCodeMap.put("0", "11111");
        morseCodeMap.put("1", "01111");
        morseCodeMap.put("2", "00111");
        morseCodeMap.put("3", "00011");
        morseCodeMap.put("4", "00001");
        morseCodeMap.put("5", "00000");
        morseCodeMap.put("6", "10000");
        morseCodeMap.put("7", "11000");
        morseCodeMap.put("8", "11100");
        morseCodeMap.put("9", "11110");

        morseCodeMap.put(".", "010101");
        morseCodeMap.put(",", "110011");
        morseCodeMap.put("?", "001100");
        morseCodeMap.put("'", "011110");
        morseCodeMap.put("!", "101011");
        morseCodeMap.put("/", "10001");
        morseCodeMap.put("(", "10110");
        morseCodeMap.put(")", "101101");
        morseCodeMap.put("&", "01000");
        morseCodeMap.put(":", "111000");
        morseCodeMap.put(";", "101010");
        morseCodeMap.put("=", "10001");
        morseCodeMap.put("+", "01010");
        morseCodeMap.put("-", "100001");
        morseCodeMap.put("_", "001101");
        morseCodeMap.put("\"", "010010");
        morseCodeMap.put("$", "0001001");
        morseCodeMap.put("@", "011010");

        // on fait 1100 - valeurSlider comme ça quand on slide vers la droite on augmente la vitesse
        // et vers la gauche on diminue la vitesse
        vitessePoint = 1100 - (int) sliderVitesse.getValue();

        /**
         * Listener du changement de vitesse sur la SlideBar
         * S'occupe de changer la vitesse d'un point (vitessePoint) en millisecondes
         */
        sliderVitesse.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                vitessePoint = 1100 - (int) sliderVitesse.getValue();
            }
        });

        // lancement de la requête de demande de permissions de la caméra
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Fonction qui s'occupe de lancer le signal en Morse (fonction générale)
     * @param v : View cliquée
     */
    public void lancerSignalMorse(View v) {
        // récupération du texte à traduire
        texteATraduire = edtTexteATraduire.getText().toString();
        /* si boucle de flash en train de tourner alors on la désactive
            sinon on lance la boucle (et on check la permission de la caméra)
        */
        if (boucleActive) {
            boucleActive = false;
            stopLoopAllumerEteindre();
        } else {
            boucleActive = true;
            if (permissionOK) {
                try {
                    // récupération de la séquence à suivre en Morse
                    String sequenceMorse = traducteurTextToMorse(texteATraduire);
                    buttonStartStop.setText("Arreter");
                    // lancement de la fonction récursive d'allumage/éteignage du flash
                    startLoopAllumerEteindre(sequenceMorse);
                }
                // capture d'un caractère intraduisible en Morse
                catch (UntranslatableCharacterException e) {
                    Toast.makeText(MainActivity.this, "Caractère intraduisible en Morse", Toast.LENGTH_SHORT).show();
                }
            }
            // si l'application n'a pas les droits pour la caméra
            else {
                Toast.makeText(MainActivity.this, "Permission Denied for the Camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Clic Listener
     * fonction qui lié à un texte préfait et qui récupère le contenu du texte du bouton pour lancer
     * sa traduction en Morse (au clic)
     * @param v : View cliquée
     */
    public void lancerSignalRaccourci(View v) {
        edtTexteATraduire.setText(((Button) v).getText());
        lancerSignalMorse(v);
    }

    /**
     *  Renvoie la séquence en Morse du texte donné
     *  0 = point simple
     *  1 = point long
     *  2 = espace entre 2 lettres
     *  3 = espace entre 2 mots
     * @param texteATraduire : le texte à traduire par la fonction
     * @return la séquence en Morse
     * @throws UntranslatableCharacterException : exception si le caractère est intraduisible en Morse
     */
    private String traducteurTextToMorse(String texteATraduire) throws UntranslatableCharacterException {
        StringBuilder sequence = new StringBuilder();
        // suppression des espaces inutiles
        String texteATraduireSanitized = texteATraduire.replaceAll(" {2,}", " ");
        // itération sur tout les caractères de la chaîne en paramètre
        for (int i = 0; i < texteATraduireSanitized.length(); i++) {
            // espace entre 2 mots
            if (texteATraduireSanitized.charAt(i) == ' ') {
                sequence.append(3);
            } else {
                // récupération de la séquence de 0 et 1 d'un caractère dans le dictionnaire morseCodeMap
                String codeSymbole = morseCodeMap.get(Character.toString(Character.toUpperCase(texteATraduireSanitized.charAt(i))));
                if (codeSymbole != null) {
                    sequence.append(codeSymbole);
                } else {
                    throw new UntranslatableCharacterException();
                }
            }
            // ajout d'un temps d'espace entre 2 caractères automatique
            sequence.append("2");
        }
        return sequence.toString();
    }

    /**
     * Lancement de la lecture de la séquence en Morse en paramètre
     * afin d'allumer le flash et l'éteindre en fonction des valeurs
     * rencontrées dans la séquence
     * @param sequenceMorse : séquence en Morse (donc composé de 0, 1, 2 et 3)
     */
    private void startLoopAllumerEteindre(String sequenceMorse) {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    switch (sequenceMorse.charAt(pointeurSequence)) {
                        case '0':
                            // décalage du pointeur de lecture
                            pointeurSequence += 1;
                            // allumer le flash
                            if(onlyFlash && !onlyBip){
                                flashLightOn();
                            }
                            else if(onlyBip && !onlyFlash) {
                                beep(10, vitessePoint);
                            } else if (onlyBip && onlyFlash) {
                                flashLightOn();
                                beep(10, vitessePoint);
                            }
                            // attente correspondant à un point simple
                            Thread.sleep(vitessePoint);
                            // eteindre le flash
                            flashLightOff();
                            // relancer le runnable (on avance de 1 en incrémentant le pointeur à chaque fois
                            handler.postDelayed(this, vitessePoint);
                            break;
                        case '1':
                            pointeurSequence += 1;
                            if(onlyFlash && !onlyBip){
                                flashLightOn();
                            }
                            else if(onlyBip && !onlyFlash) {
                                beep(10, vitessePoint);
                            } else if (onlyBip && onlyFlash) {
                                flashLightOn();
                                beep(10, vitessePoint);
                            }
                            Thread.sleep(vitessePoint * 3);
                            flashLightOff();
                            handler.postDelayed(this, vitessePoint);
                            break;
                        case '2':
                            pointeurSequence += 1;
                            // attente de vitessePoint*3 de base mais on fait vitessePoint*3-(vitessePoint)
                            // soit vitessePoint*3 - vitessePoint car ce temps d'attente est tout le temps réalisé
                            handler.postDelayed(this, vitessePoint * 2);
                            break;
                        case '3':
                            pointeurSequence += 1;
                            // attente de vitessePoint*6 de base mais on fait vitessePoint*6-(vitessePoint)
                            // soit vitessePoint*6 - vitessePoint car ce temps d'attente est tout le temps réalisé
                            handler.postDelayed(this, vitessePoint * 6);
                            break;
                    }
                }
                // interruption d'éxecution
                catch (InterruptedException ignored) {
                }
                // exception que l'on récupère quand on arrive à la fin de la séquence (normal d'y arriver)
                catch (IndexOutOfBoundsException e2) {
                    Toast.makeText(MainActivity.this, "Terminé !", Toast.LENGTH_SHORT).show();
                    stopLoopAllumerEteindre();
                }
            }
        };
        handler.post(runnable);
    }

    /**
     * Lancement de la lecture de la séquence en Morse en paramètre
     * afin de réaliser des bips plus ou moins longs en fonction des valeurs rencontrées dans la séquence
     * @param sequenceMorse : séquence en Morse (donc composé de 0, 1, 2 et 3)
     */

    /**
     * Arrete la loop du flash, réinitialise le pointeurDeSequence et le handler et libère le ToneGenerator
     */
    private void stopLoopAllumerEteindre() {
        buttonStartStop.setText("Lancer");
        pointeurSequence = 0;
        boucleActive = false;
        releaseToneGenerator();
        handler.removeCallbacks(runnable);
    }

    /**
     * Récupération des droits d'utilisation de la caméra
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionOK = true;
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied for the Camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Allume le flash
     */
    private void flashLightOff() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException ignored) {
        }
    }

    /**
     * Eteint le flash
     */
    private void flashLightOn() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
        } catch (CameraAccessException ignored) {
        }
    }

    /**
     * Fonction activant le bip sonore au volume donnée et de durée donnée en paramètres
     * @param volume : volume du son joué
     * @param duration : durée du son joué
     */
    private void beep(int volume, int duration) {
        try {
            // vérifie que le ToneGenerator n'existe pas, le crée sinon
            if (toneG == null) {
                toneG = new ToneGenerator(AudioManager.STREAM_MUSIC, volume);
            }
            // lancement du son
            toneG.startTone(ToneGenerator.TONE_DTMF_P, duration);
        } catch (Exception ignored) {
        }
    }

    /**
     * Fonction de libération du ToneGenerator pour éviter des crashs de l'appli
     */
    private void releaseToneGenerator() {
        if (toneG != null) {
            toneG.release();
            toneG = null;
        }
    }

    /**
     * Gère le changement d'état du switch de gestion du flash
     * @param v : la vue switchFlash
     */
    public void clickSwitchFlash(View v){
        // mettre le mode onlyFlash sur on ou off
        onlyFlash = switchFlash.isChecked();
    }

    /**
     * Gère le changement d'état du switch de gestion du bip
     * @param v : la vue switchBip
     */
    public void clickSwitchBip(View v){
        // mettre le mode onlyFlash sur on ou off
        onlyBip = switchBip.isChecked();
    }
}