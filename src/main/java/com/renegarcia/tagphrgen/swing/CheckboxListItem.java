/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.renegarcia.tagphrgen.swing;

import com.renegarcia.tagphrgen.Sentence;

/**
 *
 * @author Rene
 */
public class CheckboxListItem
{
        private Sentence sentence;
        private boolean isSelected = true;

        public CheckboxListItem(Sentence label)
        {
            this.sentence = label;
        }

        public boolean isSelected()
        {
            return isSelected;
        }

        public void setSelected(boolean isSelected)
        {
            this.isSelected = isSelected;
        }
        
        public Sentence getSentence()
        {
            return sentence;
        }

        @Override
        public String toString()
        {
            return sentence.tagalog;
        }
}

