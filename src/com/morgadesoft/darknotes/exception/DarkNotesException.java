package com.morgadesoft.darknotes.exception;

public class DarkNotesException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DarkNotesException() {
		super();
	}

	public DarkNotesException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public DarkNotesException(String detailMessage) {
		super(detailMessage);
	}

	public DarkNotesException(Throwable throwable) {
		super(throwable);
	}
	
}
