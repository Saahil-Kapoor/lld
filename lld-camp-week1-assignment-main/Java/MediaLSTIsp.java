// MediaLSPIsp.java
// Messy starter: Fat interface + LSP surprises (violates ISP + LSP)

import java.util.stream.Stream;

interface Player {
    void play(String source);
    void pause();
    //void record(String destination);   // many can't
    //void streamLive(String url);       // many can't
    //void download(String sourceUrl);   // many can't
}

interface Downloade{
    void download(String sourceUrl);
}

interface StreamLive{
    void streamLive(String url);
    boolean isLive();
}

interface Record{
    void record(String destination);
}

class AudioPlayer implements Player, Downloade {
    private boolean playing = false;

    @Override public void play(String source) { playing = true; }
    @Override public void pause() { playing = false; }
    @Override public void download(String sourceUrl) {
        // pretend
    }

    public boolean isPlaying() { return playing; }
}
class CameraStreamer implements StreamLive{
    private boolean liveStarted  = false;
    @Override 
    public void streamLive(String url){
        liveStarted = true;
    }
    @Override
    public boolean isLive(){
        return liveStarted;
    }
}

class CameraStreamPlayer implements Player ,Record{
    private boolean playing = false;
    StreamLive streamer;
    public CameraStreamPlayer(StreamLive streamer){
        this.streamer = streamer;
    }


    @Override public void play(String source) {
        // Surprise: needs streamLive first for “real” play
        if (!streamer.isLive()) {
            streamer.streamLive(source);
        }
        playing = true;
    }
    @Override public void pause() { playing = false; }
    @Override public void record(String destination) {
        // pretend
    }

    public boolean isPlaying() { return playing; }
}

public class MediaLSTIsp {
    public static void main(String[] args) {
        Player ap = new AudioPlayer();
        ap.play("song.mp3");
        System.out.println("Audio playing: " + ((AudioPlayer)ap).isPlaying());
        ap.pause();


        StreamLive camStreamer = new CameraStreamer();

        Player cam = new CameraStreamPlayer(camStreamer);
        cam.play("rtsp://camera");       // warning surprise// required order
        
    }
}
