package ch.FOW_Collection.domain.models;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

import ch.FOW_Collection.domain.utils.MultiLanguageString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardType extends MultiLanguageString implements Entity, Serializable {
    public static final String COLLECTION = "cardType";

    @Exclude
    private String id;
}