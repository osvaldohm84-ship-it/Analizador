package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.content.*;import android.database.sqlite.*;import android.graphics.*;import android.view.*;import android.widget.*;import java.util.*;
public class CumplimientoActivity extends Activity{
LinearLayout grillaDias;
TextView lblDiasSeleccionados,txtResultados;
Button btnCategoria,btnTurno,btnCalcular;
Set<Integer> diasSeleccionados=new HashSet<>();
String categoriaActual="FIJO";
String turnoActual="AMBOS";
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
findViewById(R.id.btnVolver).setOnClickListener(v->finish());
crearGrillaDias();
actualizarLblDias();
btnCategoria.setOnClickListener(v->elegirCategoria());
btnTurno.setOnClickListener(v->elegirTurno());
btnCalcular.setOnClickListener(v->calcular());
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
}).show();
}
void elegirTurno(){
new AlertDialog.Builder(this).setTitle("Turno:").setItems(TURNOS_DISPLAY,(d,w)->{
turnoActual=TURNOS[w];
btnTurno.setText("Turno: "+TURNOS_DISPLAY[w]);
}).show();
}
void calcular(){
if(diasSeleccionados.isEmpty()){
Toast.makeText(this,"Seleccione al menos un dia",Toast.LENGTH_SHORT).show();
return;
}
txtResultados.setText("Calculando...");
final Set<Integer> dias=new TreeSet<>(diasSeleccionados);
final String categoria=categoriaActual;
final String turno=turnoActual;
new Thread(()->{
try{
DBHelperAux db=new DBHelperAux(this);
CumplimientoResultado r=CumplimientoEngine.calcular(db.getReadableDatabase(),dias,categoria,turno);
db.close();
final String resumen=CumplimientoEngine.resumenCorto(r);
runOnUiThread(()->txtResultados.setText(resumen));
}catch(final Exception e){
runOnUiThread(()->txtResultados.setText("Error: "+e.toString()));
}
}).start();
}
static class DBHelperAux extends SQLiteOpenHelper{
DBHelperAux(Context c){super(c,"loteria.db",null,4);}
public void onCreate(SQLiteDatabase d){}
public void onUpgrade(SQLiteDatabase d,int o,int n){}
}
}
