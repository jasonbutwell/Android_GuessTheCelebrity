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

    Bitmap celebrityImage = null;

    boolean inPlay;

    private void showStartButton( boolean showStatus ) {
        Button showButton = (Button) findViewById(R.id.playButton);
        ImageView imageView = (ImageView) findViewById(R.id.celebrityImageView);

        if (showStatus == false)
        {
            showButton.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
        }
        else {
            showButton.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    public void startQuiz( View view ) {
        inPlay = true;
        showStartButton( false );
    }

    public void selectionMade(View view ) {
        if ( inPlay )
            checkAnswer(view);
    }

    private void setAnswerButtons() {
        for (int i=0; i < numberOfAnswers; i++ )
            answerButtons[i].setText(answers.get(i));
    }

    private void setAnswerStatus() {
        TextView answerTotalView = (TextView)findViewById(R.id.QuestionNumberTextView);
        answerTotalView.setText("Celebrities correct so far: " + correctAnswers +" / " + answerTotal);
    }

    private void setAnswers() {
        answers.clear();

        // get the correct answer location
        correctAnswerLocation = getRandomAnswerLocation();

        // get the right celebrity name
        String answerName = getCelebrityName(celebrityIndex);
        String otherAnswer = "";

        for (int i=0; i < numberOfAnswers; i++ ) {
            if ( i == correctAnswerLocation )             // If the index is equal to the answer location, just add in the answer
                answers.add(answerName);
            else {                                                          // if its not the correct answer location
                otherAnswer = getCelebrityName(getRandomCelebrityIndex());  // get another random celebrity name

                // while the answer we have grabbed is the same as the answer, pick another one
                while (otherAnswer.isEmpty() || otherAnswer.equals(answerName)) {
                    otherAnswer = getCelebrityName(getRandomCelebrityIndex());
                }

                // Needed to check the other answer with the other answers we currently have
                for (int j=i; j < answers.size(); j++ )                             // loop for size of answers so far
                    while ( answers.get(j).equals(otherAnswer) )                    // if our other answer is already there
                        otherAnswer = getCelebrityName(getRandomCelebrityIndex());  // get another one

                answers.add(otherAnswer);       // Once we are happy add the other answer to the list
            }
        }

        setAnswerStatus();
        setAnswerButtons();
    }

    private void checkAnswer(View view) {
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

    private int getRandomCelebrityIndex() {
        Random rand = new Random();
        return rand.nextInt(maxNumberOfCelebrities);
    }

    private int getRandomAnswerLocation() {
        Random rand = new Random();
        return rand.nextInt(numberOfAnswers);
    }

    private String getCelebrityName( int index ) {
        return celebNames.get(index);
    }

    private String getCelebrityImageURL( int index ) {
        return celebImages.get(index);
    }

    private boolean setViewImageBitmap(Bitmap bitmap) {
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

        inPlay = false;
        showStartButton( true );

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
