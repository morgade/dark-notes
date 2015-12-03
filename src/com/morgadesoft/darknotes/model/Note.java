package com.morgadesoft.darknotes.model;

import java.io.Serializable;
import java.util.Date;

public class Note implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer id;
	private String name;
	private Integer pieceCount = 0;
	private Date creationDate;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Integer getPieceCount() {
		return pieceCount;
	}

	public void setPieceCount(Integer pieceCount) {
		this.pieceCount = pieceCount;
	}
}
