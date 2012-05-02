/**
 * 
 */
package precision;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.apache.commons.math.dfp.Dfp;
import org.apache.commons.math.dfp.DfpField;
import org.apache.commons.math.dfp.DfpMath;

public class DecimalRoutines {

	private MathContext mc;
	private int scale;
	/**
	 * @return the scale
	 */
	public int getScale() {
		return scale;
	}

	/**
	 * @param scale the scale to set
	 */
	public void setScale(int scale) {
		this.scale = scale; 
	}

	public DecimalRoutines () {
		mc = new MathContext(MathContext.DECIMAL32.getPrecision(), RoundingMode.HALF_UP);
		scale = 5;
	}
	
	public static BigDecimal getDecimal(String val) {
		MathContext mc = new MathContext(MathContext.DECIMAL32.getPrecision(),
				RoundingMode.HALF_UP);
		return new BigDecimal(val,  mc);
	}
	/** 
	 * @author Ashwani Rao
	 */
	static public class   ExtendedDfp extends Dfp {
		/**
		 * @param field
		 */
		ExtendedDfp(DfpField field) {
			super(field);
			// TODO Auto-generated constructor stub
		}
		/**
		 * 
		 * @param field
		 * @param val
		 */
		 ExtendedDfp(DfpField field, double val) {
			super(field, val);
		}
	}
	
	public DecimalRoutines (int precision, int scale) {
		mc = new MathContext(precision, RoundingMode.HALF_UP);
		this.scale = scale;
	}
	
	public DecimalRoutines (int scale) {
		mc = new MathContext(MathContext.DECIMAL32.getPrecision(), RoundingMode.HALF_UP);
		this.scale = scale;
	}
	
	public DecimalRoutines (int precision, int scale, RoundingMode rmode) {
		mc = new MathContext(precision, rmode);
		this.scale = scale;
	}
	/**
	 * Compare bd1 with bd2.  Takes into account null values. If a null for any of the argument then
	 * that argument is considered as bigger. this will raise exception if both the
	 *  values are null. 
	 * @param bd1
	 * @param bd2
	 * @return: return 0 if both are equal. less than 0 if bd1 is less than bd2
	 *  else greater than zero if it is greater.
	 */
	public int compare(BigDecimal bd1, BigDecimal  bd2) {
		if (bd1 == null)
			return 1;
		else if (bd2 == null)
			return -1;
		return bd1.compareTo(bd2); 
	}
	
	/**
	 * Subtract the second argument from the first. Takes into account the mathematical context and 
	 * scale of this class.
	 * @param bd
	 * @param subtrahend
	 * @return
	 */
	public BigDecimal differerence(BigDecimal bd, BigDecimal subtrahend) {
		return bd.subtract(subtrahend,mc).setScale(this.scale, mc.getRoundingMode());
	}
	
	public BigDecimal absoluteDifference(BigDecimal bd, BigDecimal subtrahend) {
		return bd.subtract(subtrahend,mc).abs(mc).setScale(this.scale, mc.getRoundingMode());
	}
	
	public BigDecimal divide(BigDecimal bd, BigDecimal divisor) {
		return bd.divide(divisor, this.mc);
	}
	public BigDecimal divide(BigDecimal bd, BigDecimal divisor, int scale) {
		return bd.divide(divisor, this.mc).setScale(scale, this.mc.getRoundingMode());
	}
	public BigDecimal pow(BigDecimal base, int exponent) {
		return base.pow(exponent, this.mc);
	}
	
	public BigDecimal pow(BigDecimal base, int exponent, int scale) {
		return base.pow(exponent, this.mc).setScale(scale, this.mc.getRoundingMode());
	}
	public BigDecimal multipy(BigDecimal base, BigDecimal multiplicand ) {
		return base.multiply(multiplicand, this.mc);
	}
	/**
	 * Compute power. Base raise to exponent. 
	 * @param base: the base value
	 * @param exp : raise to value
	 * @param type: calculation to be done on the int value or the float value or the double value
	 * of the arguments. If it is 1 then int values of the base and exp are considered. If it is
	 * 2 then float values and double values when it is 3. For any other values it defaults to double.
	 * @return
	 */
	public BigDecimal pow(BigDecimal base, BigDecimal exp,  int type) {
		ExtendedDfp bs = null;
		ExtendedDfp ep = null;
		switch (type) {
		case 1: bs = new ExtendedDfp(new DfpField(10),base.intValue());
				ep = new ExtendedDfp(new DfpField(10),exp.intValue());
				break;
		case 2: 
			bs = new ExtendedDfp(new DfpField(10),base.floatValue());
			ep = new ExtendedDfp(new DfpField(10),exp.floatValue());
			break;
		case 3:
			default :
				bs = new ExtendedDfp(new DfpField(10),base.doubleValue());
				ep = new ExtendedDfp(new DfpField(10),exp.doubleValue());				
		}
		return new BigDecimal(DfpMath.pow(bs,ep).toDouble(),this.mc);
	}
	
	/**
	 * returns a natural logratihimic of BigDecimal value
	 * @param val
	 * @return
	 */
	public BigDecimal natularLog(BigDecimal val) {
		 return new BigDecimal(DfpMath.log(new ExtendedDfp(new DfpField(10), val.doubleValue())).toDouble(), this.mc);
	}
}
