package com.jasonbutwell.guessthecelebrity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
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
    private CountDownTimer timer;

    // For the pattern matching
    private Pattern p;
    private Matcher m;

    private List <String>celebImages;
    private List <String>celebNames;
    private List <String>answers;
    private List <String>celebsSoFar;

    // location of correct answer and the index of the celeb we need to pull (namewise and imagewise)
    private int correctAnswerLocation = 0;
    private int celebrityIndex = 0;

    // answers user has got correct and the total answers that have been answered
    private int correctAnswers = 0;
    private int answerTotal = 0;
    private int quizDurationSeconds = 60;
    private int milliSecond = 1000;

    // storage for image bitmap that we need to fetch
    private Bitmap celebrityImage = null;

    // game state
    private boolean inPlay;

    // count down timer view
    private TextView timerTextView;

    // clears all the answer buttons so they are blank prior to and after play
    private void clearAnswerButtons() {
        for (int i=0; i < answerButtons.length; i++) {
            answerButtons[i].setText("");
        }
    }

    // The quiz has ended so reset everything
    private void endQuiz() {
        inPlay = false;
        timerTextView.setText("Time Left: "+quizDurationSeconds+"s");
        showStartButton(true);
        clearAnswerButtons();
        timer.cancel();
    }

    // Toggle between start button and image view
    private void showStartButton( boolean showStatus ) {
        Button showButton = (Button) findViewById(R.id.playButton);
        ImageView imageView = (ImageView) findViewById(R.id.celebrityImageView);

        if (showStatus == false)
        {
            // Show == false means turn off show button and enable the imageview
            showButton.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
        }
        else {
            // show == true means turn off the imageview and show the button
            showButton.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    // start the quiz. enabled with the start button is tapped
    public void startQuiz( View view ) {

        correctAnswers = 0;
        answerTotal = 0;

        inPlay = true;
        celebsSoFar.clear();

        showStartButton( false );
        getNextCelebrity();

        timer = new CountDownTimer(quizDurationSeconds*milliSecond+(milliSecond/10),milliSecond) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText("Time Left: "+String.valueOf(millisUntilFinished/milliSecond)+"s");
            }

            @Override
            public void onFinish() {
                endQuiz();
            }
        }.start();
    }

    // called on answer button tap
    public void selectionMade(View view ) {
        if ( inPlay == true )
            checkAnswer(view);
    }

    // sets the buttons to the answers we stored
    private void setAnswerButtons() {
        for (int i=0; i < numberOfAnswers; i++ )
            answerButtons[i].setText(answers.get(i));
    }

    // updates the answer view to show how many answered and how many correct
    private void setAnswerStatus() {
        TextView answerTotalView = (TextView)findViewById(R.id.QuestionNumberTextView);
        answerTotalView.setText("Celebrities correct so far: " + correctAnswers +" / " + answerTotal);
    }

    // sets up the answers for the user
    private void setAnswers() {
        answers.clear();

        // get the correct answer location
        correctAnswerLocation = getRandomAnswerLocation();

        // get the right celebrity name
        String answerName = getCelebrityName(celebrityIndex);
        String otherAnswer = "";

        for (int i=0; i < numberOfAnswers; i++ ) {
            if ( i == correctAnswerLocation ) {            // If the index is equal to the answer location, just add in the answer
                answers.add(answerName);
            }
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

        // update the answer status and the answer buttons
        setAnswerStatus();
        setAnswerButtons();
    }

    // called when we tap a button to check the tag of the button with the answer id to see if correct or incorrect
    private void checkAnswer(View view) {
        String message = "";

        if (view.getTag().toString().equals(Integer.toString(correctAnswerLocation+1))) {
            message = "Well done! CORRECT!";        // Answer correct
            correctAnswers++;
        }
        else
            message = "I'm sorry, that's WRONG!";   // Answer wrong

        // show correct / incorrect to user
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // check we have not answered more questions than we have
        if ( answerTotal < maxNumberOfCelebrities ) {
            answerTotal++;
        }

        getNextCelebrity();

        // check to see if all the celebs have been guessed. If so, we need to end the game!
        if ( answerTotal == maxNumberOfCelebrities )
            endQuiz();
    }

    // Gets a random index based on the number of celebrities
    private int getRandomCelebrityIndex() {
        Random rand = new Random();
        return rand.nextInt(maxNumberOfCelebrities);
    }

    // gets a random location to store the correct answer in
    private int getRandomAnswerLocation() {
        Random rand = new Random();
        return rand.nextInt(numberOfAnswers);
    }

    // Passes back the name based on the index
    private String getCelebrityName( int index ) {
        return celebNames.get(index);
    }

    // Passes back the URL of the celeb image based on the index
    private String getCelebrityImageURL( int index ) {
        return celebImages.get(index);
    }

    // Change the celeb image view to the bitmap we obtained
    private boolean setViewImageBitmap(Bitmap bitmap) {
        boolean status = false;

        if ( bitmap != null ) {
            ImageView view = (ImageView) findViewById(R.id.celebrityImageView);
            view.setImageBitmap(bitmap);
            status = true;
        }

        return status;
    }

    // Uses pattern matching to extract the name and url of the celebrities from the HTML and store them in 2 seperate lists
    private void extractDataFromHTML() {
        p = Pattern.compile(HTMLextractionPattern);
        m = p.matcher(resultHTML);

        String [] array = new String[5];    // for storing the broken up string
        int count = 0;

        // stop when we have found all the celebrities, including a counter just to be safe
        while ( m.find() && count < maxNumberOfCelebrities ) {
            String temp = m.group(1);
            array = temp.split( HTMLsplitString );

            celebImages.add(array[0]); // store the extracted celebrity image url
            celebNames.add(array[1]); // store the extracted celebrity name

            // Increasing the count of celebs we have extracted so far
            count++;
        }
    }

    // Called to grab the next celebrity that the user can guess
    private void getNextCelebrity() {

        celebrityIndex = getRandomCelebrityIndex();

        // Look to ensure we have unique entries and no celebrity is repeated on each game
        for ( int i =0; i < celebsSoFar.size(); i++ )
            while ( String.valueOf(celebrityIndex).equals(celebsSoFar.get(i) ) )
                celebrityIndex = getRandomCelebrityIndex();

        // If the celebrities index has not been used before, add it to the list to check against next time.
        celebsSoFar.add(String.valueOf(celebrityIndex));

        imageTask = new ImageDownloaderTask();

        // exception handling
        try {
            celebrityImage = imageTask.execute(getCelebrityImageURL(celebrityIndex)).get();
            setViewImageBitmap(celebrityImage);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // set up the answer buttons
        setAnswers();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up lists
        celebImages = new ArrayList<String>();
        celebNames = new ArrayList<String>();
        answers = new ArrayList<String>();
        celebsSoFar = new ArrayList<String>();

        // store the answer button references in an array for easy reference throughout
        answerButtons = new Button[numberOfAnswers];
        answerButtons[0] = (Button)findViewById(R.id.button1);
        answerButtons[1] = (Button)findViewById(R.id.button2);
        answerButtons[2] = (Button)findViewById(R.id.button3);
        answerButtons[3] = (Button)findViewById(R.id.button4);

        // needed to update the view of the timer
        timerTextView = (TextView) findViewById(R.id.TimertextView);

        // quiz initialise
        inPlay = false;
        showStartButton( true );
        clearAnswerButtons();

        // Async Task to grab the HTML from the web in the background
        task = new DownloadHTMLTask();

        // Exception handling
        try {
            resultHTML = task.execute(htmlURL).get();
            extractDataFromHTML();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
