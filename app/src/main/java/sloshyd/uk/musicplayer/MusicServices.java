package sloshyd.uk.musicplayer;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import java.net.URI;
import java.util.ArrayList;

/**
 * Created by Darren Brooks on 03/03/2015.
 * A Service is an application component representing either an application's desire
 * to perform a longer-running operation while not interacting with the user or to supply
 * functionality for other applications to use. Each service class must have a corresponding
 * <service> declaration in its package's AndroidManifest.xml.
 * Services can be started with Context.startService() and Context.bindService().
 *
 * Also require to implement three additional interfaces http://developer.android.com/reference/android/media/MediaPlayer.html
 * has more information and detail of the States a player can be in
 * The list of songs is passed into our Service
 */
public class MusicServices extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener
{


    // media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosn;
    //Base interface for a remotable object, the core part of a lightweight remote procedure call
    // mechanism designed for high performance when performing in-process and cross-process calls.
    // This interface describes the abstract protocol for interacting with a remotable object.
    // Do not implement this interface directly, instead extend from Binder.
    //A bound service is the server in a client-server interface. A bound service allows
    // components (such as activities) to bind to the service, send requests, receive responses,
    // and even perform interprocess communication (IPC). A bound service typically lives only
    // while it serves another application component and does not run in the background indefinitely.
    private final IBinder musicBind = new MusicBinder();


    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }



    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    public void onCreate()  {
        //create the Service
        super.onCreate();
        songPosn=0;
        //create player
        player = new MediaPlayer();
        initMusicPlayer();
    }

    public void initMusicPlayer(){
        //set music player properties http://developer.android.com/reference/android/media/MediaPlayer.html
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

    }

    public void playSong(){
        player.reset();
        //get song
        Song playSong = songs.get(songPosn);
        long currSong = playSong.getId();
        //set uri
        Uri trackUri = ContentUris.withAppendedId
                (MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch (Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source");
        }

        player.prepareAsync();
        //Successful invoke of this method in a valid state transfers
        // the object to the Preparing state.
        // (see http://developer.android.com/reference/android/media/MediaPlayer.html)

    }

    public void setList(ArrayList<Song> theSongs){
        songs=theSongs;
    }

    public void setSong(int songIndex){
        songPosn = songIndex;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
    //start playback
        mp.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }


    public class MusicBinder extends Binder {
        MusicServices getMusicService(){

            return MusicServices.this;

        }
    }
}
