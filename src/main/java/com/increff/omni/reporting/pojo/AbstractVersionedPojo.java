package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import javax.persistence.PreUpdate;
import javax.persistence.Version;
import java.time.ZonedDateTime;

@Setter
@Getter
@MappedSuperclass
// Todo correct constraints
public class AbstractVersionedPojo {

	@Version
	private int version;

	private ZonedDateTime createdAt = ZonedDateTime.now();

	private ZonedDateTime updatedAt = ZonedDateTime.now();

	@PreUpdate
	public void setUpdatedAt() {
		this.updatedAt = ZonedDateTime.now();
	}

}
