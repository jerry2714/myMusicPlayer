package ledcubeproject.musicplayer;

import android.content.Context;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user123.musicplayer.R;

import java.util.Hashtable;

import ledcubeproject.ui.ColorPickerDialog;
import ledcubeproject.ui.SimpleDialog;

/**
 * Created by Jerry on 2017/8/6.
 */

public class EffectSettingDialog extends SimpleDialog {
    final int[] cold = {0x40007F, 0x000088, 0x004B97 , 0x00E3E3};//000079,004B97
    final int[] warm = {0xFF0000, 0xF75000, 0xFF8000, 0xF9F900};
    final int[] colorful = {0x2894FF, 0x28FF28, 0xF9F900, 0xFF77FF};
    final int[][] colorSet = {cold, warm, colorful};
    final int[] colorId = {R.id.cold, R.id.warm, R.id.colorful};
    final Hashtable<Integer, int[]> colorSetTable = new Hashtable<>();
    final String[] patternName = {"無", "core", "tree"};
    final Hashtable<String, Integer> patternTable = new Hashtable<>();
    LedCubeMotion ledCubeMotion;
    ColorPickerDialog colorPickerDialog = null;
    private RadioGroup rgColorSet, rgPatternSet;
    private TextView txvPatternColor;
    private int patternColor = 0;

    public EffectSettingDialog(Context c, int viewId, String title, LedCubeMotion lm) {
        super(c, viewId, title);
        ledCubeMotion = lm;
        ledCubeMotion.setColors(warm);
        ledCubeMotion.setPattern(0, 0);

        rgColorSet = (RadioGroup) getView().findViewById(R.id.colorSet);
        rgPatternSet = (RadioGroup) getView().findViewById(R.id.patternSet);
        txvPatternColor = (TextView) getView().findViewById(R.id.patternColor);

        for (int i = 0; i < colorId.length; i++)
            colorSetTable.put(colorId[i], colorSet[i]);
        for (int i = 0; i < patternName.length; i++) {
            patternTable.put(patternName[i], i);
            RadioButton rb = new RadioButton(c);
            rb.setText(patternName[i]);
            rgPatternSet.addView(rb);
            if (i == 0)
                rb.setChecked(true);
        }


        rgColorSet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int[] r = colorSetTable.get(checkedId);
                if (r != null)
                    ledCubeMotion.setColors(r);
            }
        });
        rgPatternSet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                int r = patternTable.get(rb.getText().toString());
                ledCubeMotion.setPattern(r, patternColor);
               // Toast.makeText(getContext(), "" + r, Toast.LENGTH_SHORT).show();
            }
        });

        txvPatternColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (colorPickerDialog == null)
                    colorPickerDialog = new ColorPickerDialog(getContext(), 0, "", new ColorPickerDialog.OnColorChangedListener() {
                        @Override
                        public void colorChanged(int color) {
                            txvPatternColor.setBackgroundColor(color);
                            patternColor = color;
                            ledCubeMotion.setPatternColor(color);
                            //Toast.makeText(getContext(), ""+Integer.toHexString(color), Toast.LENGTH_SHORT).show();
                        }
                    });
                colorPickerDialog.show();
            }
        });


    }
}
