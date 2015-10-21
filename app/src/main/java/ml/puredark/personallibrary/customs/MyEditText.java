package ml.puredark.personallibrary.customs;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.widget.EditText;

public class MyEditText extends EditText {
    private Animatable startIcon, endIcon;
    public MyEditText(Context context) {
        super(context);
    }

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setStartIcon(Animatable startIcon){
        this.startIcon = startIcon;
        this.setBackground((Drawable) startIcon);
    }

    public void setEndIcon(Animatable endIcon){
        this.endIcon = endIcon;
    }

    public void start(){
        if(startIcon!=null){
            this.setBackground((Drawable) startIcon);
            startIcon.start();
        }
    }

    public void reverse(){
        if(endIcon!=null){
            this.setBackground((Drawable)endIcon);
            endIcon.start();
        }
    }

}
