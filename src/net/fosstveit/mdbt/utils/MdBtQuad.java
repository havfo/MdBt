package net.fosstveit.mdbt.utils;

import java.io.Serializable;

public class MdBtQuad implements Serializable {

	private static final long serialVersionUID = -4417235999119569256L;
	private String[] tokens;
    private boolean canStart = false;
    private boolean canEnd = false;
	
	public MdBtQuad(String s1, String s2, String s3, String s4) {
        tokens = new String[]{s1, s2, s3, s4};
    }
    
    public String getToken(int index) {
        return tokens[index];
    }
    
    public void setCanStart(boolean flag) {
        canStart = flag;
    }
    
    public void setCanEnd(boolean flag) {
        canEnd = flag;
    }    
    
    public boolean canStart() {
        return canStart;
    }
    
    public boolean canEnd() {
        return canEnd;
    }
    
    public int hashCode() {
        return tokens[0].hashCode() +
               tokens[1].hashCode() +
               tokens[2].hashCode() +
               tokens[3].hashCode();
    }
    
    public boolean equals(Object o) {
        MdBtQuad other = (MdBtQuad) o;
        return other.tokens[0].equals(tokens[0]) &&
               other.tokens[1].equals(tokens[1]) &&
               other.tokens[2].equals(tokens[2]) &&
               other.tokens[3].equals(tokens[3]);
    }
}