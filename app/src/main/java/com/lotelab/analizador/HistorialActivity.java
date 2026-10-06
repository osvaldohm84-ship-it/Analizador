package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.database.sqlite.*;import android.view.*;import android.widget.*;import android.graphics.*;import java.util.*;
public class HistorialActivity extends Activity{
LinearLayout listaHistorial;
TextView infoHistorial;
EditText buscarFecha;
Button btnBuscar,btnLimpiar,btnCargarMas,btnOrden;
List<Draw> todos=new ArrayList<>();
List<Draw> filtrados=new ArrayList<>();
int mostrados=0;
boolean descendente=true;
static final int POR_PAGINA=1000;
@Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_historial);
listaHistorial=findViewById(R.id.listaHistorial);
infoHistorial=findViewById(R.id.infoHistorial);
buscarFecha=findViewById(R.id.buscarFecha);
btnBuscar=findViewById(R.id.btnBuscar);
btnLimpiar=findViewById(R.id.btnLimpiar);
btnCargarMas=findViewById(R.id.btnCargarMas);
btnOrden=findViewById(R.id.btnOrden);
btnOrden.setText("↓ Recientes");
new Thread(()->{
todos=leerTodos();
filtrados=new ArrayList<>(todos);
runOnUiThread(()->mostrarPagina());
}).start();
btnBuscar.setOnClickListener(v->filtrar());
btnLimpiar.setOnClickListener(v->{buscarFecha.setText("");filtrados=new ArrayList<>(todos);mostrados=0;mostrarPagina();});
btnCargarMas.setOnClickListener(v->{mostrados+=POR_PAGINA;mostrarPagina();});
btnOrden.setOnClickListener(v->{descendente=!descendente;btnOrden.setText(descendente?"↓ Recientes":"↑ Antiguos");mostrados=0;mostrarPagina();});
}
void filtrar(){
String q=buscarFecha.getText().toString().trim();
if(q.isEmpty()){filtrados=new ArrayList<>(todos);mostrados=0;mostrarPagina();return;}
List<Draw> res=new ArrayList<>();
for(Draw d:todos){if(d.date.contains(q))res.add(d);}
filtrados=res;mostrados=0;mostrarPagina();
}
void mostrarPagina(){
listaHistorial.removeAllViews();
int total=filtrados.size();
if(total==0){infoHistorial.setText("No hay sorteos para mostrar");btnCargarMas.setVisibility(View.GONE);return;}
int limite=Math.min(mostrados+POR_PAGINA,total);
for(int i=0;i<limite;i++){
Draw d;
if(descendente){
d=filtrados.get(i);
}else{
d=filtrados.get(total-1-i);
}
LinearLayout fila=new LinearLayout(this);
fila.setOrientation(LinearLayout.VERTICAL);
fila.setPadding(10,8,10,8);
fila.setBackgroundColor(i%2==0?Color.parseColor("#FFFFFF"):Color.parseColor("#F5F5F5"));
TextView tvFecha=new TextView(this);
tvFecha.setText(d.date+"  |  "+d.tn);
tvFecha.setTextSize(14);
tvFecha.setTextColor(Color.parseColor("#1A237E"));
tvFecha.setTypeface(null,Typeface.BOLD);
fila.addView(tvFecha);
LinearLayout nums=new LinearLayout(this);
nums.setOrientation(LinearLayout.HORIZONTAL);
nums.setPadding(0,4,0,0);
nums.addView(numTV("Cent: "+d.cent,"#4E342E"));
nums.addView(numTV("F: "+d.f,"#1565C0"));
nums.addView(numTV("C1: "+d.c1,"#2E7D32"));
nums.addView(numTV("C2: "+d.c2,"#EF6C00"));
fila.addView(nums);
listaHistorial.addView(fila);
}
String info="Mostrando "+limite+" de "+total+" sorteos";
if(!buscarFecha.getText().toString().isEmpty())info+=" (filtrados)";
infoHistorial.setText(info);
if(limite<total)btnCargarMas.setVisibility(View.VISIBLE);
else btnCargarMas.setVisibility(View.GONE);
}
TextView numTV(String txt,String color){
TextView tv=new TextView(this);
tv.setText(txt);
tv.setTextSize(12);
tv.setTextColor(Color.parseColor(color));
tv.setTypeface(null,Typeface.BOLD);
LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
tv.setLayoutParams(lp);
return tv;
}
List<Draw> leerTodos(){
List<Draw> lista=new ArrayList<>();
DB db=new DB(this);
Cursor c=db.allDesc();
while(c.moveToNext())lista.add(new Draw(c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getString(6)));
c.close();
return lista;
}
static class Draw{String date,tn,cent,f,c1,c2;Draw(String a,String b,String c,String d,String e,String f){date=a;tn=b;cent=c;this.f=d;c1=e;c2=f;}}
static class DB extends SQLiteOpenHelper{DB(Context c){super(c,"loteria.db",null,1);}public void onCreate(SQLiteDatabase d){}public void onUpgrade(SQLiteDatabase d,int o,int n){}Cursor all(){return getReadableDatabase().query("draws",null,null,null,null,null,"id ASC");}Cursor allDesc(){return getReadableDatabase().query("draws",null,null,null,null,null,"id DESC");}}
}
