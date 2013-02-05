package mpi.eudico.client.annotator.timeseries;

/**
 * Implementation of a time series data track. Data are stored in a flat list
 * or array of values; in combination with the  (fixed) sample rate it is
 * possible to find a time-value pair.
 */
public class ContinuousRateTSTrack extends AbstractTSTrack {
    private float sampleRate;
    private float msPerSample;
    private float[] data;

    /**
     * Constructor.
     */
    public ContinuousRateTSTrack() {
        super();
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#setSampleRate(int)
     */
    public void setSampleRate(float rate) {
        sampleRate = rate;
        msPerSample = 1000 / sampleRate;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getSampleRate()
     */
    public float getSampleRate() {
        return sampleRate;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getSampleCount()
     */
    public int getSampleCount() {
        if (data == null) {
            return 0;
        }

        return data.length;
    }

    /**
     * Returns an array of floats.
     *
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getData()
     */
    public Object getData() {
        return data;
    }

    /**
     * Sets the data of this tracks. Currently this method only accepts an
     * array of floats; in any other case  an IllegalArgumentException will be
     * thrown.
     *
     * @param data DOCUMENT ME!
     *
     * @throws IllegalArgumentException when the data is provided in anything
     *         else but an array of floats
     *
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#setData(java.lang.Object)
     */
    public void setData(Object data) {
        if (!(data instanceof float[])) {
            throw new IllegalArgumentException(
                "This track only accepts an array of floats");
        }

        this.data = (float[]) data;
    }

    /**
     * Returns a calculated index based on the time, time offset and the sample
     * rate. There is no guarantee that the index is within the data array's
     * bounds.
     *
     * @see mpi.eudico.client.annotator.timeseries.AbstractTSTrack#getIndexForTime(long)
     */
    public int getIndexForTime(long time) {
        int index = -1;

        if ((time + timeOffset) >= 0) {
            //index = (int) ((time + timeOffset) / msPerSample);	
            /* this might be more accurate and more consistent */
            index = (msPerSample >= 1)
                ? (int) ((time + timeOffset) / msPerSample)
                : (int) Math.ceil((time + timeOffset) / msPerSample);
        }

        return index;
    }

    /**
     * @see mpi.eudico.client.annotator.timeseries.AbstractTSTrack#getTimeForIndex(int)
     */
    public long getTimeForIndex(int index) {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(
                "Index should be greater than or equal to 0");
        }

        long time = 0L;

        if (data != null) {
            if (index >= data.length) {
                throw new ArrayIndexOutOfBoundsException("Index (" + index +
                    ") is greater than " + (data.length - 1));
            }

            time = (long) (index * msPerSample);
        }
        // HS Jan 2011 return time - timeOffset instead of time + timeOffset?
        return time - timeOffset;
    }

    /**
     * Returns Float.NaN if there are no (valid) values within the range.
     * 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getAverage(long,
     *      long)
     */
    public float getAverage(long begin, long end) {
        if (data == null || data.length == 0) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException("Begin time " + begin + " < 0");
        }

        // try to calculate an average anyway, for the part that does exist in the interval
//        if (end > (msPerSample * data.length)) {
//            // in fact: end + timeOffset > msPerSample * data.length - timeOffset
//            throw new ArrayIndexOutOfBoundsException("End time greater than track duration: " + end + " > " +
//                (msPerSample * data.length));
//        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi > data.length - 1) {
        	return Float.NaN;
        }
        if (ei > data.length - 1) {
        	ei = data.length - 1;
        }
        
        if (bi == ei) {
        	long time = getTimeForIndex(bi);
        	if (begin <= time - timeOffset && end >= time - timeOffset) {
        		return data[bi];
        	} else {
        		if (time - timeOffset < begin) {
        			if (bi < data.length - 1) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi + 1])) {
        					return (data[bi] + data[bi + 1]) / 2;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// time - timeOffset > end
        			if (bi > 0) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi - 1])) {
        					return (data[bi] + data[bi - 1]) / 2;
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        int count = 0;
        float total = 0f;

        for (int i = bi; (i <= ei) && (i < data.length); i++) {
        	if (!Float.isNaN(data[i])) {
        		total += data[i];
        		count++;
        	}
        }

        if (count == 0) {
            return Float.NaN;
        } else {
            return total / count;
        }
    }

    /**
     * Returns NaN in case no valid values are in the range.
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getMaximum(long,
     *      long)
     */
    public float getMaximum(long begin, long end) {
        if (data == null || data.length == 0) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException("Begin time " + begin + " < 0");
        }

//        if (end > (msPerSample * data.length)) {
//            // in fact: end + timeOffset > msPerSample * data.length - timeOffset
//            throw new ArrayIndexOutOfBoundsException("End time greater than track duration: " + end + " > " +
//                (msPerSample * data.length));
//        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi > data.length - 1) {
        	return Float.NaN;
        }
        if (ei > data.length - 1) {
        	ei = data.length - 1;
        }
        
        if (bi == ei) {
        	long time = getTimeForIndex(bi);
        	if (begin <= time - timeOffset && end >= time - timeOffset) {
        		return data[bi];
        	} else {
        		if (time - timeOffset < begin) {
        			if (bi < data.length - 1) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi + 1])) {
        					return data[bi] > data[bi + 1] ? data[bi] : data[bi + 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// time - timeOffset > end
        			if (bi > 0) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi - 1])) {
        					return data[bi] > data[bi - 1] ? data[bi] : data[bi - 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        float max = Integer.MIN_VALUE; //problem with Float.MIN_VALUE
        int count = 0;

        for (int i = bi; (i <= ei) && (i < data.length); i++) {
            if (!Float.isNaN(data[i])) {
            	if (data[i] > max) {
            		max = data[i];
            	}
                count++;
            }
        }
        
        if (count > 0) {
        	return max;
        } else {
        	return Float.NaN;
        }
    }

    /**
     * Returns NaN in case there are no (valid) values in the range.
     * 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getMinimum(long,
     *      long)
     */
    public float getMinimum(long begin, long end) {
        if (data == null || data.length == 0) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException("Begin time " + begin + " < 0");
        }

//        if (end > (msPerSample * data.length)) {
//            // in fact: end + timeOffset > msPerSample * data.length - timeOffset
//            throw new ArrayIndexOutOfBoundsException("End time greater than track duration: " + end + " > " +
//                (msPerSample * data.length));
//        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi > data.length - 1) {
        	return Float.NaN;
        }
        if (ei > data.length - 1) {
        	ei = data.length - 1;
        }
        
        if (bi == ei) {
        	long time = getTimeForIndex(bi);
        	if (begin <= time - timeOffset && end >= time - timeOffset) {
        		return data[bi];
        	} else {
        		if (time - timeOffset < begin) {
        			if (bi < data.length - 1) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi + 1])) {
        					return data[bi] < data[bi + 1] ? data[bi] : data[bi + 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// time - timeOffset > end
        			if (bi > 0) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi - 1])) {
        					return data[bi] < data[bi - 1] ? data[bi] : data[bi - 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        float min = Integer.MAX_VALUE;
        int count = 0;

        for (int i = bi; (i <= ei) && (i < data.length); i++) {
            if (!Float.isNaN(data[i])) {
            	if (data[i] < min) {
            		min = data[i];
            	}
                count++;
            }
        }
        
        if (count > 0) {
        	return min;
        } else {
        	return Float.NaN;
        }
    }
    
    /**
     * Returns NaN in case there are no (valid) values in the range.
     * 
     * @see mpi.eudico.client.annotator.timeseries.TimeSeriesTrack#getSum(long,
     *      long)
     */
    public float getSum(long begin, long end) {
    	if (data == null || data.length == 0) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException("Begin time " + begin + " < 0");
        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi > data.length - 1) {
        	return Float.NaN;
        }
        if (ei > data.length - 1) {
        	ei = data.length - 1;
        }
        
        if (bi == ei) {
        	long time = getTimeForIndex(bi);
        	if (begin <= time - timeOffset && end >= time - timeOffset) {
        		return data[bi];
        	} else {
        		if (time - timeOffset < begin) {
        			if (bi < data.length - 1) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi + 1])) {
        					return data[bi] + data[bi + 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// time - timeOffset > end
        			if (bi > 0) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi - 1])) {
        					return data[bi] + data[bi - 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        float sum = 0;
        int count = 0;
        
        for (int i = bi; (i <= ei) && (i < data.length); i++) {
            if (!Float.isNaN(data[i])) {
            	sum += data[i];
                count++;
            }
        }
        
        if (count > 0) {
        	return sum;
        } else {
        	return Float.NaN;
        }
    }

	public float getValueAtBegin(long begin, long end) {
		if (data == null || data.length == 0) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException("Begin time " + begin + " < 0");
        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi > data.length - 1) {
        	return Float.NaN;
        }
        if (ei > data.length - 1) {
        	ei = data.length - 1;
        }
        
        if (bi == ei) {
        	long time = getTimeForIndex(bi);
        	if (begin <= time - timeOffset && end >= time - timeOffset) {
        		return data[bi];
        	} else {
        		if (time - timeOffset < begin) {
        			if (bi < data.length - 1) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi + 1])) {
        					return data[bi];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// time - timeOffset > end
        			if (bi > 0) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi - 1])) {
        					return data[bi - 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        return data[bi];
	}

	public float getValueAtEnd(long begin, long end) {
		if (data == null || data.length == 0) {
            return 0; // throw an exception?
        }

        if (begin >= end) {
            throw new IllegalArgumentException(
                "Begin time should not be greater than or equal to the end time");
        }

        if (begin < 0) {
            throw new ArrayIndexOutOfBoundsException("Begin time " + begin + " < 0");
        }

        int bi = getIndexForTime(begin);
        int ei = getIndexForTime(end);
        
        if (bi > data.length - 1) {
        	return Float.NaN;
        }
        if (ei > data.length - 1) {
        	ei = data.length - 1;
        }
        
        if (bi == ei) {
        	long time = getTimeForIndex(bi);
        	if (begin <= time - timeOffset && end >= time - timeOffset) {
        		return data[bi];
        	} else {
        		if (time - timeOffset < begin) {
        			if (bi < data.length - 1) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi + 1])) {
        					return data[bi + 1];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		} else {// time - timeOffset > end
        			if (bi > 0) {
        				if (!Float.isNaN(data[bi]) && !Float.isNaN(data[bi - 1])) {
        					return data[bi];
        				} else {
        					return Float.NaN;
        				}
        			} else {
        				return Float.NaN;
        			}
        		}
        	}
        }
        
        return data[ei];
	}
}
