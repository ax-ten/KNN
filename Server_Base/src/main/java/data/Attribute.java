package data;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.io.Serializable;

/**
 * Modella un generico attributo discreto o continuo.
 * @Author Giannotte Giampiero
 */
public abstract class Attribute implements Serializable{
    private final String name;
    private final int index;

    /**
     * @param name nome simbolico dell'attributo
     * @param index identificativo numerico dell'attributo
     */
    public Attribute(String name, int index) {
        this.name = name;
        this.index = index;
    }

    /**
     * @return il valore del membro name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return il valore del membro index
     */
    public int getIndex() {
        return this.index;
    }
}
