package com.gnuc.thoth.framework.callbacks;

public interface ThothRequestCallback
{	
	void handle(int resultCode);
	void progress(int resultCode,int progressValue,int reference);
}
