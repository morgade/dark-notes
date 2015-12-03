package com.morgadesoft.darknotes.model.piece;

import com.morgadesoft.darknotes.model.NotePiece;

public abstract class BaseNotePiece implements NotePiece{
	private static final long serialVersionUID = 1L;
	protected final long timestamp;
	
	public BaseNotePiece() {
		this.timestamp = System.currentTimeMillis();
	}

	public long getTimeStamp() {
		return timestamp;
	}
	
}
