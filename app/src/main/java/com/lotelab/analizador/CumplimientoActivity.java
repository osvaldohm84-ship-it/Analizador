package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.content.*;import android.database.sqlite.*;import android.graphics.*;import android.text.*;import android.view.*;import android.widget.*;import java.util.*;
public class CumplimientoActivity extends Activity{
LinearLayout grillaDias;
TextView lblDiasSeleccionados,txtResultados;
Button btnCategoria,btnTurno,btnCalcular,btnGuardar;
Set<Integer> diasSeleccionados=new HashSet<>();
String categoriaActual="FIJO";
String turnoActual="AMBOS";
CumplimientoResultado ultimoResultado=null;
static final String[] CATEGORIAS={"FIJO","C1","C2","CENTENA","DECENA","TERMINAL"};
static final String[] TURNOS={"AMBOS","T","N"};
static final String[] TURNOS_DISPLAY={"AMBOS","TARDE","NOCHE"};
static final int[] TAMANOS={10,15,20,25,30};
@Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_cumplimiento);
grillaDias=findViewById(R.id.grillaDias);
lblDiasSeleccionados=findViewById(R.id.lblDiasSeleccionados);
txtResultados=findViewById(R.id.txtResultados);
btnCategoria=findViewById(R.id.btnCategoria);
btnTurno=findViewById(R.id.btnTurno);
btnCalcular=findViewById(R.id.btnCalcular);
btnGuardar=findViewById(R.id.btnGuardar);
findViewById(R.id.btnVolver).setOnClickListener(v->finish());
crearGrillaDias();
actualizarLblDias();
btnCategoria.setOnClickListener(v->elegirCategoria());
btnTurno.setOnClickListener(v->elegirTurno());
btnCalcular.setOnClickListener(v->calcular());
btnGuardar.setOnClickListener(v->guardarComoMetodo());
}
void crearGrillaDias(){
grillaDias.removeAllViews();
for(int fila=0;fila<5;fila++){
LinearLayout fl=new LinearLayout(this);
fl.setOrientation(LinearLayout.HORIZONTAL);
fl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
for(int col=0;col<7;col++){
int dia=fila*7+col+1;
if(dia>31)break;
Button btn=new Button(this);
btn.setText(String.valueOf(dia));
btn.setTextSize(13);
btn.setPadding(0,0,0,0);
final int d=dia;
btn.setOnClickListener(v->{if(diasSeleccionados.contains(d))diasSeleccionados.remove(d);else diasSeleccionados.add(d);crearGrillaDias();actualizarLblDias();});
LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
lp.setMargins(2,2,2,2);
btn.setLayoutParams(lp);
if(diasSeleccionados.contains(dia)){btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#8BC34A")));btn.setTextColor(Color.WHITE);}
else{btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));btn.setTextColor(Color.parseColor("#00897B"));}
fl.addView(btn);
}
grillaDias.addView(fl);
}
}
void actualizarLblDias(){
if(diasSeleccionados.isEmpty()){lblDiasSeleccionados.setText("Dias: (ninguno)");return;}
List<Integer> lista=new ArrayList<>(diasSeleccionados);
Collections.sort(lista);
StringBuilder sb=new StringBuilder("Dias: ");
for(int i=0;i<lista.size();i++){sb.append(lista.get(i));if(i<lista.size()-1)sb.append(", ");}
lblDiasSeleccionados.setText(sb.toString());
}
void elegirCategoria(){
new AlertDialog.Builder(this).setTitle("Categoria:").setItems(CATEGORIAS,(d,w)->{
categoriaActual=CATEGORIAS[w];
btnCategoria.setText("Categoria: "+categoriaActual);
ultimoResultado=null;
btnGuardar.setVisibility(View.GONE);
}).show();
}
void elegirTurno(){
new AlertDialog.Builder(this).setTitle("Turno:").setItems(TURNOS_DISPLAY,(d,w)->{
turnoActual=TURNOS[w];
btnTurno.setText("Turno: "+TURNOS_DISPLAY[w]);
ultimoResultado=null;
btnGuardar.setVisibility(View.GONE);
}).show();
}
void calcular(){
if(diasSeleccionados.isEmpty()){
Toast.makeText(this,"Seleccione al menos un dia",Toast.LENGTH_SHORT).show();
return;
}
txtResultados.setText("Calculando...");
btnGuardar.setVisibility(View.GONE);
final Set<Integer> dias=new TreeSet<>(diasSeleccionados);
final String categoria=categoriaActual;
final String turno=turnoActual;
new Thread(()->{
try{
DBHelperAux db=new DBHelperAux(this);
CumplimientoResultado r=CumplimientoEngine.calcular(db.getReadableDatabase(),dias,categoria,turno);
db.close();
ultimoResultado=r;
final String resumen=CumplimientoEngine.resumenCorto(r);
runOnUiThread(()->{
txtResultados.setText(resumen);
if(!r.pools.isEmpty())btnGuardar.setVisibility(View.VISIBLE);
});
}catch(final Exception e){
runOnUiThread(()->txtResultados.setText("Error: "+e.toString()));
}
}).start();
}
void guardarComoMetodo(){
if(ultimoResultado==null||ultimoResultado.pools.isEmpty()){
Toast.makeText(this,"Primero calcule un metodo",Toast.LENGTH_SHORT).show();
return;
}
final String[] tamanosTxt=new String[TAMANOS.length];
for(int i=0;i<TAMANOS.length;i++)tamanosTxt[i]="Top "+TAMANOS[i];
new AlertDialog.Builder(this).setTitle("Tamano del pool:").setItems(tamanosTxt,(d,w)->{
final int tamano=TAMANOS[w];
pedirNombre(tamano);
}).show();
}
void pedirNombre(final int tamano){
LinearLayout ll=new LinearLayout(this);
ll.setOrientation(LinearLayout.VERTICAL);
ll.setPadding(20,20,20,20);
final EditText et=new EditText(this);
String sugerido=categoriaActual+" dias "+ultimoResultado.diasSeleccionados.toString()+" Top"+tamano;
et.setText(sugerido);
ll.addView(et);
new AlertDialog.Builder(this).setTitle("Nombre del metodo:").setView(ll).setPositiveButton("Guardar",(d,w)->{
String nombre=et.getText().toString().trim();
if(nombre.isEmpty())nombre=sugerido;
guardarMetodo(nombre,tamano);
}).setNegativeButton("Cancelar",null).show();
}
void guardarMetodo(String nombre,int tamano){
CumplimientoResultado.PoolN pool=null;
for(CumplimientoResultado.PoolN p:ultimoResultado.pools){
if(p.tamano==tamano){pool=p;break;}
}
if(pool==null){
Toast.makeText(this,"Tamano no encontrado",Toast.LENGTH_SHORT).show();
return;
}
Metodo m=new Metodo();
m.nombre=nombre;
m.categoria=ultimoResultado.categoria;
m.turno=ultimoResultado.turno;
int bitmask=0;
for(int dia:ultimoResultado.diasSeleccionados){bitmask|=(1<<(dia-1));}
m.diasBitmask=bitmask;
m.tamanoPool=pool.numeros.size();
StringBuilder csv=new StringBuilder();
for(int i=0;i<pool.numeros.size();i++){
if(i>0)csv.append(",");
csv.append(pool.numeros.get(i));
}
m.numerosPoolCsv=csv.toString();
m.estado=calcularEstado(pool.rachaActual,pool.mesesFallados);
m.rachaActual=pool.rachaActual;
m.mayorRacha=pool.mayorRacha;
m.ultimoFallo=pool.ultimoFallo;
m.mesesConsecutivosFallando=calcularMesesFallando();
m.totalMeses=ultimoResultado.mesesTotales;
m.totalCumplidos=pool.mesesCumplidos;
m.totalFallados=pool.mesesFallados;
long id=MetodoManager.guardar(this,m);
if(id>0){
int total=MetodoManager.contar(this);
String aviso="";
if(total>20)aviso="\n\nAviso: tienes "+total+" metodos guardados. Considera limpiar.";
Toast.makeText(this,"Metodo guardado. Total: "+total+aviso,Toast.LENGTH_LONG).show();
}else{
Toast.makeText(this,"Error al guardar",Toast.LENGTH_SHORT).show();
}
}
int calcularMesesFallando(){
if(ultimoResultado==null)return 0;
CumplimientoResultado.PoolN pool=null;
for(CumplimientoResultado.PoolN p:ultimoResultado.pools){
if(p.tamano==ultimoResultado.pools.get(0).tamano){pool=p;break;}
}
if(pool==null)return 0;
return pool.rachaActual==0?1:0;
}
String calcularEstado(int racha,int fallos){
if(racha==0)return "ALERTA";
if(racha<=2)return "RIESGO";
return "CUMPLIENDO";
}
static class DBHelperAux extends SQLiteOpenHelper{
DBHelperAux(Context c){super(c,"loteria.db",null,5);}
public void onCreate(SQLiteDatabase d){}
public void onUpgrade(SQLiteDatabase d,int o,int n){}
}
}
