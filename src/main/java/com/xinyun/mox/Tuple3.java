package com.xinyun.mox;

public class Tuple3<T1, T2, T3> {
	
	public T1 get_1() {
		return _1;
	}

	public T2 get_2() {
		return _2;
	}

	public T3 get_3() {
		return _3;
	}

	private final T1 _1;
	private final T2 _2;
	private final T3 _3;
	
	public Tuple3(final T1 _1, final T2 _2, final T3 _3) {
		this._1 = _1;
		this._2 = _2;
		this._3 = _3;
	}


}
