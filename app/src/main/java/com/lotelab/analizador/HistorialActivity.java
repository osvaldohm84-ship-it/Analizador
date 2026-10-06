package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.database.sqlite.*;import android.view.*;import android.widget.*;import android.graphics.*;import java.util.*;
public class HistorialActivity extends Activity{
LinearLayout listaHistorial;
TextView infoPagina,filtroActivo;
Button btnAnterior,btnSiguiente,btnLupa;
List<Draw> todos=new ArrayList<>();
List<Draw> filtrados=new ArrayList<>();
int paginaActual=0;
String filtro="";
static final int POR_PAGINA=100;
@Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_historial);
listaHistorial=findViewById(R.id.listaHistorial);
infoPagina=findViewById(R.id.infoPagina);
filtroActivo=findViewById(R.id.filtroActivo);
btnAnterior=findViewById(R.id.btnAnterior);
btnSiguiente=findViewById(R.id.btnSiguiente);
btnLupa=findViewById(R.id.btnLupa);
new Thread(()->{
todos=leerTodos();
filtrados=new ArrayList<>(todos);
runOnUiThread(()->mostrarPagina());
}).start();
btnAnterior.setOnClickListener(v->{if(paginaActual>0){paginaActual--;mostrarPagina();}});
btnSiguiente.setOnClickListener(v->{int totalPag=(filtrados.size()+POR_PAGINA-1)/POR_PAGINA;if(paginaActual<totalPag-1){paginaActual++;mostrarPagina();}});
btnLupa.setOnClickListener(v->abrirBusqueda());
}
void abrirBusqueda(){
LinearLayout l=new LinearLayout(this);
l.setOrientation(LinearLayout.VERTICAL);
l.setPadding(20,20,20,20);
TextView info=new TextView(this);
info.setText("Escriba la fecha a buscar (ej: 06-10-2026)");
info.setTextSize(14);
info.setPadding(0,0,0,20);
l.addView(info);
EditText input=new EditText(this);
input.setHint("dd-MM-yyyy");
input.setText(filtro);
l.addView(input);
new AlertDialog.Builder(this).setTitle("Buscar por fecha").setView(l).setPositiveButton("Buscar",(d,w)->{
filtro=input.getText().toString().trim();
if(filtro.isEmpty()){filtrados=new ArrayList<>(todos);filtroActivo.setVisibility(View.GONE);}
else{
List<Draw> res=new ArrayList<>();
for(Draw dr:todos){if(dr.date.contains(filtro))res.add(dr);}
filtrados=res;
filtroActivo.setText("Filtro: "+filtro+" ("+res.size()+" resultados)");
filtroActivo.setVisibility(View.VISIBLE);
}
paginaActual=0;
mostrarPagina();
}).setNegativeButton("Cancelar",null).setNeutralButton("Limpiar",(d,w)->{
filtro="";filtrados=new ArrayList<>(todos);filtroActivo.setVisibility(View.GONE);paginaActual=0;mostrarPagina();
}).show();
}
void mostrarPagina(){
listaHistorial.removeAllViews();
int total=filtrados.size();
if(total==0){infoPagina.setText("Pag: 0/0");listaHistorial.addView(mensaje("No hay sorteos para mostrar"));return;}
int totalPag=(total+POR_PAGINA-1)/POR_PAGINA;
if(paginaActual>=totalPag)paginaActual=totalPag-1;
int inicio=paginaActual*POR_PAGINA;
int fin=Math.min(inicio+POR_PAGINA,total);
for(int i=inicio;i<fin;i++){
Draw d=filtrados.get(i);
listaHistorial.addView(crearFila(d));
}
infoPagina.setText("Pag: "+(paginaActual+1)+"/"+totalPag);
btnAnterior.setEnabled(paginaActual>0);
btnSiguiente.setEnabled(paginaActual<totalPag-1);
}
View crearFila(Draw d){
LinearLayout cont=new LinearLayout(this);
cont.setOrientation(LinearLayout.VERTICAL);
cont.setPadding(12,10,12,10);
int colorFondo=(d.tn.equals("T"))?Color.parseColor("#F5F5F5"):Color.parseColor("#E8EAF6");
cont.setBackgroundColor(colorFondo);
LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
lp.setMargins(0,2,0,2);
cont.setLayoutParams(lp);
LinearLayout filaSup=new LinearLayout(this);
filaSup.setOrientation(LinearLayout.HORIZONTAL);
filaSup.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
TextView tvFecha=new TextView(this);
tvFecha.setText(fechaBonita(d.date));
tvFecha.setTextSize(15);
tvFecha.setTextColor(Color.parseColor("#0D47A1"));
tvFecha.setTypeface(null,Typeface.BOLD);
tvFecha.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1));
filaSup.addView(tvFecha);
TextView tvTurno=new TextView(this);
tvTurno.setText(d.tn.equals("T")?"🌞":"🌙");
tvTurno.setTextSize(22);
filaSup.addView(tvTurno);
cont.addView(filaSup);
LinearLayout filaNum=new LinearLayout(this);
filaNum.setOrientation(LinearLayout.HORIZONTAL);
filaNum.setPadding(0,8,0,0);
filaNum.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
filaNum.addView(caja(d.cent,"#FFFFFF","#0D47A1"));
filaNum.addView(caja(d.f,"#E65100","#FFFFFF"));
filaNum.addView(caja(d.c1,"#FFFFFF","#0D47A1"));
filaNum.addView(caja(d.c2,"#FFFFFF","#0D47A1"));
cont.addView(filaNum);
return cont;
}
TextView caja(String txt,String bg,String fg){
TextView tv=new TextView(this);
tv.setText(txt);
tv.setTextSize(20);
tv.setTextColor(Color.parseColor(fg));
tv.setTypeface(null,Typeface.BOLD);
tv.setGravity(Gravity.CENTER);
tv.setBackgroundColor(Color.parseColor(bg));
LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
lp.setMargins(3,0,3,0);
tv.setLayoutParams(lp);
tv.setPadding(8,8,8,8);
return tv;
}
String fechaBonita(String fecha){
try{
String[] p=fecha.split("[-/]");
int dia=Integer.parseInt(p[0]);
int mes=Integer.parseInt(p[1]);
int anio=Integer.parseInt(p[2]);
String[] meses={"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
Calendar cal=Calendar.getInstance();
cal.set(anio,mes-1,dia);
String[] dias={"Dom","Lun","Mar","Mié","Jue","Vie","Sáb"};
String diaSem=dias[cal.get(Calendar.DAY_OF_WEEK)-1];
return diaSem+". "+String.format("%02d",dia)+" "+meses[mes-1]+"/"+anio;
}catch(Exception e){return fecha;}
}
TextView mensaje(String txt){
TextView tv=new TextView(this);
tv.setText(txt);
tv.setTextSize(14);
tv.setPadding(20,20,20,20);
tv.setGravity(Gravity.CENTER);
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
static class DB extends SQLiteOpenHelper{DB(Context c){super(c,"loteria.db",null,2);}public void onCreate(SQLiteDatabase d){}public void onUpgrade(SQLiteDatabase d,int o,int n){}Cursor allDesc(){return getReadableDatabase().query("draws",null,null,null,null,null,"id DESC");}}
}
