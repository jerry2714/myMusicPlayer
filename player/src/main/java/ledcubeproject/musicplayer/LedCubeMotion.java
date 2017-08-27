package ledcubeproject.musicplayer;

import ledcubeproject.models.ledcube.LedCubeDataModel;
import ledcubeproject.models.ledcubecontroller.BluetoothLedCubeController;
import ledcubeproject.models.ledcubecontroller.LedCubeController;
import ledcubeproject.models.musicprocessor.Player;
import ledcubeproject.models.musicprocessor.processor.SimpleSpectrumAnalyzer;
import ledcubeproject.models.util.Callback;

/**
 * Created by Jerry on 2017/8/6.
 */

public class LedCubeMotion implements Callback {
    SimpleSpectrumAnalyzer simpleSpectrumAnalyzer;
    Player player;
    BluetoothLedCubeController ledCubeController;
    LedCubeDataModel ledCubeDataModel;
    int x = 0, y = 0, z = 0;
    long t;
    int[] colors;
    int patternNum = 0;
    int patternColor = 0;

    public LedCubeMotion(Player p, SimpleSpectrumAnalyzer spectrumAnalyzer, BluetoothLedCubeController lc, LedCubeDataModel ld) {
        player = p;
        simpleSpectrumAnalyzer = spectrumAnalyzer;
        ledCubeController = lc;
        ledCubeDataModel = ld;
    }

    @Override
    public void run() {
        x = y = z = 0;
        try {
            int[] spectrum = simpleSpectrumAnalyzer.getOutput(player.getCurrentPCM(), player.getSampleRate());
            if (System.nanoTime() - t < 1000000000 / 15)
                return;
            t = System.nanoTime();
            if (spectrum == null) return;
            ledCubeController.setBackground(false, patternNum, patternColor);
            for (int i = 0; i < spectrum.length; i++) {

                int p1 = ledCubeDataModel.coordinateConvert(x, y, z);
                //兩點座標相同時，該點的LED應該點亮，而此狀況應有7階段，包括不亮以及6顆燈6個階段，
                //故判斷不亮時不加入命令，並且p2的z座標都減1，以符合座標只有0~5的狀況
                if (spectrum[i] != 0) {
                    int p2 = ledCubeDataModel.coordinateConvert(x, y, spectrum[i] - 1);
                    int cIdx = i / (spectrum.length / colors.length);
                    cIdx = cIdx < colors.length ? cIdx : colors.length - 1;
                    ledCubeController.line(false, p1, p2, colors[cIdx]);
                }
                coordinateChange(6);
            }
            ledCubeController.addToQueue(ledCubeController.command(LedCubeController.OUTPUT, 0, 0));
            ledCubeController.sendQueue();
        } catch (Exception e) {
            ledCubeController.clean();
            e.printStackTrace();
        }
    }

    public void coordinateChange(int sideLength) {
        if (x < (sideLength - 1) && y == 0)
            x++;
        else if (x == (sideLength - 1) && y < (sideLength - 1))
            y++;
        else if (x > 0 && y == (sideLength - 1))
            x--;
        else if (x == 0 && y <= (sideLength - 1))
            y--;
    }

    public void setColors(int[] c) {
        colors = c;
    }

    public void setPattern(int n, int color) {
        patternNum = n;
        patternColor = color;
    }

    public void setPatternColor(int color) {
        patternColor = color;
    }
}
