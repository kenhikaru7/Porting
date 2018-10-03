package org.isf.utils.jobjects;

import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.ui.TextField;
import com.vaadin.shared.ui.ValueChangeMode;

public class NumberField extends TextField implements ValueChangeListener<String>  {

    public String lastValue;

    public NumberField() {
        setValueChangeMode(ValueChangeMode.EAGER);
        addValueChangeListener(this);
        lastValue="";
    }

    @Override
    public void valueChange(ValueChangeEvent<String> event) {
        String text = (String) event.getValue();
        if(text.equals(""))
            lastValue="";
        else{
            try {
                new Double(text);
                lastValue = text;
            } catch (NumberFormatException e) {
                setValue(lastValue);
            }
        }

    }
}