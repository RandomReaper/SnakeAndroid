package dickclock.team.snake;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filters implements InputFilter {

    enum Type{CHANCE, NUMBER}
    Pattern mPattern;
    public Filters(Type type) {
        String regex = "";
        switch (type){
            case CHANCE: regex = "^[2-9]([0-9]{0,2})$"; break;
            case NUMBER: regex = "^\\d{0,2}$"; break;
            default: return;
        }
        mPattern=Pattern.compile(regex);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        Matcher matcher=mPattern.matcher(dest.toString()+source);
        if(!matcher.matches())
            return "";
        return null;
    }
}
