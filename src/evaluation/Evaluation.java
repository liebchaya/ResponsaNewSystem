package evaluation;

import java.util.HashSet;
import java.util.LinkedList;


public class Evaluation<E> {

	LinkedList<E> m_resultsList = null;
	HashSet<E> m_relevant = null;
	HashSet<E> m_retrieved = null;
	double m_precision=-1, m_recall=-1, m_f1=-1, m_ap=-1, m_looseRate=-1;
	String m_name = null;
	
	
	public Evaluation(LinkedList<E> resultsList, HashSet<E> relevant, String name) {
		m_name = name;
		m_resultsList = resultsList;
		m_relevant = relevant;		
		m_retrieved = new HashSet<E>();
		for (E w : resultsList)
			m_retrieved.add(w);
		if (m_retrieved.size()==0){
			//EL.info("0 "+m_name+" retrievd . evaluation stopped");
			System.out.println("0 "+m_name+" retrievd . evaluation stopped");
			m_precision=0;
			m_recall=0;
			m_f1=0;
			m_ap=0;
			m_looseRate=0;
		}
		else{
			//System.out.println(m_retrieved.size()+" groups retrievd for term "+m_name);
			evaluate();
		}
	}
	
	private void evaluate(){
		calcPrecision();
		calcRecall();
		calcF1();
		calcAveragePrecision();
		calcLooseRate();
}

private double calcPrecision() {
//		HashSet<E> intersection = new HashSet<E>(m_relevant);
//		intersection.retainAll(m_retrieved);
		HashSet<E> intersection = new HashSet<E>(m_retrieved);
		intersection.retainAll(m_relevant);
	
		double numerator = intersection.size();
		double denominator = m_retrieved.size();
		for(E c:intersection)
			System.out.println(c.toString());
		double p = numerator/denominator;
		//EL.info("precision = "+p);
		setPrecision(p);
		return p;
}

private double calcRecall() {
//	HashSet<E> intersection = new HashSet<E>(m_relevant);
//	intersection.retainAll(m_retrieved);
	
	HashSet<E> intersection = new HashSet<E>(m_retrieved);
	intersection.retainAll(m_relevant);
	
	double numerator = intersection.size();
	double denominator = m_relevant.size();
	double r = numerator/denominator;
	//EL.info("recall = "+r);
	setRecall(r);
	return r;
}
private double calcF1() {
	double p = getPrecision();
	double r = getRecall();
	double f1 = (2*p*r)/(p+r);
	//EL.info("f1 = "+f1);
	setF1(f1);
	return f1;
}

private int inRel(int i){
	if (m_relevant.contains(m_resultsList.get(i)))
			return 1;
	else return 0;				
}
private double countRelAt(int i){
	double sum=0;
	for (int j= 0; j<=i; j++)
		sum= sum + inRel(j);
	return sum;
}
private double calcAveragePrecision() {
	double sum = 0;
	//double r = getRecall();
	int retCount = m_retrieved.size();
	for (int i=0; i<retCount; i++)
		sum = sum + ((inRel(i)*countRelAt(i))/(i+1));
	//double ap = (1/r)*sum;
	//01/08/10//fix ap denomenator//double ap = (1/(countRelAt(retCount-1)))*sum;
	double denominator = m_relevant.size();
	double ap = (1/(denominator))*sum;
	//EL.info("average precision = "+ap);
	setAp(ap);
	return ap;
}
/**
 * Calculate looseRate for Macro Averaging
 * @return
 */
private double calcLooseRate() {
	double numerator = 0;
	for(E element:m_retrieved){
		HashSet<E> intersection = new HashSet<E>(m_relevant);
		HashSet<E> elemSet = new HashSet<E>();
		elemSet.add(element);
		intersection.retainAll(elemSet);
		if(intersection.size()>1)
			numerator += intersection.size()-1;
	}
	double denominator = m_relevant.size();
	double r = numerator/denominator;
	setLooseRate(r);
	return r;
}

/**
 * Get lost number for Micro Averaging
 * @return
 */
public int getLostClustersNum() {
	int numerator = 0;
	for(E element:m_retrieved){
		HashSet<E> intersection = new HashSet<E>(m_relevant);
		HashSet<E> elemSet = new HashSet<E>();
		elemSet.add(element);
		intersection.retainAll(elemSet);
		if(intersection.size()>1)
			numerator += intersection.size()-1;
	}
	return numerator;
}
	

private double getPrecision() {
	return m_precision;
}
public String getP(){
	return String.valueOf(100*m_precision);
}
private void setPrecision(double x){
	m_precision = x;
}

private double getRecall() {
	return m_recall;
}
public String getR(){
	return String.valueOf(100*m_recall);
}
private void setRecall(double x){
	m_recall = x;
}

private double getF1() {
	return m_f1;
}
public String getF(){
	return String.valueOf(100*m_f1);
}
private void setF1(double x){
	m_f1 = x;
}

private double getAp() {
	return m_ap;
}
public String getAP(){
	return String.valueOf(100*m_ap);
}
private void setAp(double x){
	m_ap = x;
}

private void setLooseRate(double x){
	m_looseRate = x;
}

private double getLooseRate() {
	return m_looseRate;
}
public String getEvalString(){
	String str="\nprecision\treacll\tF1\taverage precision\n"
		+(100*getPrecision())+"\t"+(100*getRecall())+"\t"+(100*getF1())+"\t"+(100*getAp())+"\t"+(100*getLooseRate())+"\n";
	return str;
}

public String getTitle(){
	return "\nprecision\treacll\tF1\taverage precision\n";
}

public String getShortEvalString() {
	String str=(100*getPrecision())+"\t"+(100*getRecall())+"\t"+(100*getF1())+"\t"+(100*getAp())+"\t"+(100*getLooseRate());
	return str;
}
		
	
}
