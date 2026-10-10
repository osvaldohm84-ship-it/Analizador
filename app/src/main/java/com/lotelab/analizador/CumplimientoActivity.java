package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.content.*;import android.database.sqlite.*;import android.graphics.*;import android.view.*;import android.widget.*;import java.util.*;
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
btnGuardar.setOnClickListener(v->abrirDialogoGuardar());
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
void abrirDialogoGuardar(){
if(ultimoResultado==null||ultimoResultado.pools.isEmpty()){
Toast.makeText(this,"Primero calcule un metodo",Toast.LENGTH_SHORT).show();
return;
}
LinearLayout principal=new LinearLayout(this);
principal.setOrientation(LinearLayout.VERTICAL);
principal.setPadding(20,20,20,20);
ScrollView scroll=new ScrollView(this);
scroll.addView(principal);
TextView tvInfo=new TextView(this);
tvInfo.setText("Marca los tamanos que quieras guardar:");
tvInfo.setTextSize(13);
tvInfo.setTextColor(Color.parseColor("#0D47A1"));
tvInfo.setPadding(0,0,0,10);
principal.addView(tvInfo);
final CheckBox[] checks=new CheckBox[ultimoResultado.pools.size()];
for(int i=0;i<ultimoResultado.pools.size();i++){
CumplimientoResultado.PoolN p=ultimoResultado.pools.get(i);
checks[i]=new CheckBox(this);
checks[i].setText("Top "+p.tamano+" - "+p.mesesCumplidos+"/"+ultimoResultado.mesesTotales+" ("+String.format("%.1f",100.0*p.mesesCumplidos/ultimoResultado.mesesTotales)+"%)");
checks[i].setTextSize(13);
checks[i].setChecked(p.tamano==20||p.tamano==30);
principal.addView(checks[i]);
}
new AlertDialog.Builder(this).setTitle("Guardar metodos").setView(scroll).setPositiveButton("Guardar marcados",(d,w)->{
int guardados=0;
for(int i=0;i<checks.length;i++){
if(checks[i].isChecked()){
CumplimientoResultado.PoolN p=ultimoResultado.pools.get(i);
guardarMetodo(p);
guardados++;
}
}
int total=MetodoManager.contar(this);
String aviso="";
if(total>20)aviso="\n\nAviso: tienes "+total+" metodos. Considera limpiar.";
Toast.makeText(this,"Guardados: "+guardados+" | Total: "+total+aviso,Toast.LENGTH_LONG).show();
}).setNegativeButton("Cancelar",null).show();
}
void guardarMetodo(CumplimientoResultado.PoolN pool){
Metodo m=new Metodo();
StringBuilder dias=new StringBuilder();
for(int d:ultimoResultado.diasSeleccionados){
if(dias.length()>0)dias.append(",");
dias.append(d);
}
m.nombre=categoriaActual+" dias "+dias.toString()+" Top"+pool.tamano;
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
m.estado=calcularEstado(pool.rachaActual);
m.rachaActual=pool.rachaActual;
m.mayorRacha=pool.mayorRacha;
m.ultimoFallo=pool.ultimoFallo;
m.mesesConsecutivosFallando=pool.rachaActual==0?1:0;
m.totalMeses=ultimoResultado.mesesTotales;
m.totalCumplidos=pool.mesesCumplidos;
m.totalFallados=pool.mesesFallados;
MetodoManager.guardar(this,m);
}
String calcularEstado(int racha){
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
