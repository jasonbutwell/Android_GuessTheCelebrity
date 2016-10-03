package com.jasonbutwell.guessthecelebrity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private final String htmlURL = "http://www.posh24.com/celebrities";
    private final String HTMLsplitString = "\" alt=\"";
    private final String HTMLextractionPattern = "img src=\"(.*?)\"/>";
    private final int maxNumberOfCelebrities = 100;
    private final int numberOfAnswers = 4;

    private Button [] answerButtons;

    private String resultHTML = null;
    private DownloadHTMLTask task;
    private ImageDownloaderTask imageTask;

    private Pattern p;
    private Matcher m;

    private List <String>celebImages;
    private List <String>celebNames;
    private List <String>answers;

    int correctAnswerLocation = 0;
    int celebrityIndex = 0;

    int correctAnswers = 0;
    int answerTotal = 0;

    String celebrityIndexURL = "";
    String celebrityName = "";
    Bitmap celebrityImage = null;

    public void selectionMade(View view ) {
        //Log.i("result:", resultHTML);
        checkAnswer(view);
    }

    void setAnswerButtons() {
        for (int i=0; i < numberOfAnswers; i++ ) {
            answerButtons[i].setText(answers.get(i));
        }
    }

    void setAnswerStatus() {
        TextView answerTotalView = (TextView)findViewById(R.id.QuestionNumberTextView);
        answerTotalView.setText("Celebrities correct so far: " + correctAnswers +" / " + answerTotal);
    }

    void setAnswers() {
        answers.clear();

        correctAnswerLocation = getRandomAnswerLocation();

        String answerName = "";

        for (int i=0; i < numberOfAnswers; i++ ) {
            if ( i != correctAnswerLocation ) {

                while ( answerName.isEmpty() || answerName.equals(getCelebrityName(celebrityIndex)) )
                    answerName = getCelebrityName(getRandomCelebrityIndex());

                answers.add(getCelebrityName(getRandomCelebrityIndex()));
            }
            else
                answers.add(getCelebrityName(celebrityIndex));
        }

        setAnswerStatus();
        setAnswerButtons();
    }

    void checkAnswer(View view) {
        String message = "";

        if (view.getTag().toString().equals(Integer.toString(correctAnswerLocation+1))) {
            message = "Well done! CORRECT!";
            correctAnswers++;
        }
        else
            message = "I'm sorry, that's WRONG!";

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        answerTotal++;
        getNextCelebrity();
    }

    int getRandomCelebrityIndex() {
        Random rand = new Random();
        return rand.nextInt(maxNumberOfCelebrities);
    }

    int getRandomAnswerLocation() {
        Random rand = new Random();
        return rand.nextInt(numberOfAnswers);
    }

    String getCelebrityName( int index ) {
        return celebNames.get(index);
    }

    String getCelebrityImageURL( int index ) {
        return celebImages.get(index);
    }

    boolean setViewImageBitmap(Bitmap bitmap) {
        boolean status = false;

        if ( bitmap != null ) {
            ImageView view = (ImageView) findViewById(R.id.celebrityImageView);
            view.setImageBitmap(bitmap);
            status = true;
        }

        return status;
    }

    private void extractDataFromHTML() {
        p = Pattern.compile(HTMLextractionPattern);
        m = p.matcher(resultHTML);

        String [] array = new String[5];
        int count = 0;

        while ( m.find() && count < maxNumberOfCelebrities ) {
            String temp = m.group(1);
            array = temp.split( HTMLsplitString );

            celebImages.add(array[0]); // store the extracted celebrity image url
            celebNames.add(array[1]); // store the extracted celebrity name

            count++;
        }
    }

    private void getNextCelebrity() {
        celebrityIndex = getRandomCelebrityIndex();

        imageTask = new ImageDownloaderTask();

        try {
            celebrityImage = imageTask.execute(getCelebrityImageURL(celebrityIndex)).get();
            setViewImageBitmap(celebrityImage);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        setAnswers();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebImages = new ArrayList<String>();
        celebNames = new ArrayList<String>();
        answers = new ArrayList<String>();

        answerButtons = new Button[numberOfAnswers];
        answerButtons[0] = (Button)findViewById(R.id.button1);
        answerButtons[1] = (Button)findViewById(R.id.button2);
        answerButtons[2] = (Button)findViewById(R.id.button3);
        answerButtons[3] = (Button)findViewById(R.id.button4);

        task = new DownloadHTMLTask();

        try {
            resultHTML = task.execute(htmlURL).get();
            extractDataFromHTML();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        getNextCelebrity();

//        String name = getCelebrityName(celebrityIndex);
//        String url = getCelebrityImageURL(celebrityIndex);
//        System.out.println("#" + celebrityIndex + "name: " + name + " url:" + url);



        System.out.println(answers);
    }
}
