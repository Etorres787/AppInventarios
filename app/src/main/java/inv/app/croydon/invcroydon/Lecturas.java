package inv.app.croydon.invcroydon;

public class Lecturas {
    private int fecha;
    private int bodega;
    private int area;
    private String estante;
    private int seleccion;
    private int conteo;
    private String Ean;
    private int UndMed;
    private int Cantidad;
    private String FechaConteo;
    private int HoraConteo;

    public Lecturas(){

    }
    public Lecturas(int fecha, int bodega, int area,String estante, int seleccion, int conteo, String Ean,
                    int UndMed, int Cantidad, String FechaConteo, int HoraConteo){
        this.fecha = fecha;
        this.bodega = bodega;
        this.area = area;
        this.estante = estante;
        this.seleccion = seleccion;
        this.conteo = conteo;
        this.Ean = Ean;
        this.UndMed = UndMed;
        this.Cantidad = Cantidad;
        this.FechaConteo = FechaConteo;
        this.HoraConteo = HoraConteo;
    }

    public int getFecha(){
        return fecha;
    }
    public void setFecha(int fecha){
        this.fecha = fecha;
    }

    public int getBodega(){
        return bodega;
    }
    public void setBodega(int bodega){
        this.bodega = bodega;
    }

    public int getArea() {
        return area;
    }
    public void setArea(int area){
        this.area = area;
    }

    public String getEstante(){
        return estante;
    }
    public void setEstante(String estante){
        this.estante= estante;
    }

    public int getSeleccion(){
        return seleccion;
    }
    public void setSeleccion(int seleccion){
        this.seleccion = seleccion;
    }

    public int getConteo(){
        return conteo;
    }
    public void setConteo(int conteo){
        this.conteo = conteo;
    }

    public String getEan() {
        return Ean;
    }
    public void setEan(String ean) {
        this.Ean = ean;
    }

    public int getUndMed() {
        return UndMed;
    }
    public void setUndMed(int undMed) {
        this.UndMed = undMed;
    }

    public int getCantidad() {
        return Cantidad;
    }
    public void setCantidad(int cantidad) {
        this.Cantidad = cantidad;
    }

    public String getFechaConteo() {
        return FechaConteo;
    }
    public void setFechaConteo(String fechaConteo) {
        this.FechaConteo = fechaConteo;
    }

    public int getHoraConteo() {
        return HoraConteo;
    }

    public void setHoraConteo(int horaConteo) {
        this.HoraConteo = horaConteo;
    }


    @Override
    public String toString() {
        return "Lecturas{"+
        "fecha=" + fecha + '\'' +
        ", bodega=" + bodega + '\'' +
        ", area=" + area + '\'' +
        ", estante=" + estante + '\'' +
        ", seleccion=" + seleccion + '\'' +
        ", conteo=" + conteo + '\'' +
        ", Ean=" + Ean + '\'' +
        ", UndMed=" + UndMed + '\'' +
        ", cantidad=" + Cantidad + '\'' +
        ", FechaConteo=" + FechaConteo + '\'' +
        ", HoraConteo=" + HoraConteo + '\'' +
        '}';
    }
}
