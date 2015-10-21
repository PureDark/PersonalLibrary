package ml.puredark.personallibrary.customs;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;


public class MyFloatingActionButton extends FloatingActionButton {
    private Animatable startIcon, endIcon;
    public MyFloatingActionButton(Context context) {
        super(context);
    }

    public MyFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setStartIcon(Animatable startIcon){
        this.startIcon = startIcon;
        this.setImageDrawable((Drawable)startIcon);
    }

    public void setEndIcon(Animatable endIcon){
        this.endIcon = endIcon;
    }

    public void start(){
        if(startIcon!=null){
            this.setImageDrawable((Drawable)startIcon);
            startIcon.start();
        }
    }

    public void reverse(){
        if(endIcon!=null){
            this.setImageDrawable((Drawable)endIcon);
            endIcon.start();
        }
    }

}
