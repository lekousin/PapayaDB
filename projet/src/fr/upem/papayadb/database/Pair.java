package fr.upem.papayadb.database;

public class Pair<T1, T2> {
	private T1 value1;
	private T2 value2;
	
	public Pair(T1 v1, T2 v2){
		value1 = v1;
		value2 = v2;
	}
	
	public T1 getV1(){
		return value1;
	}
	
	public T2 getV2(){
		return value2;
	}
}
