public abstract class Attribute {
    String name; // nome simbolico dell'attributo
    int index; // identificativo numerico dell'attributo

    public Attribute(String name, int index){
        this.name = name;
        this.index = index;
    }

    // %return : nome dell'attributo;
    public String getName(){
        return name;
    }

    // %return : identificativo dell'attributo;
    public int getIndex(){
        return index;
    }



}
