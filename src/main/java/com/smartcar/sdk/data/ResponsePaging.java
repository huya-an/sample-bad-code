package com.smartcar.sdk.data;

/** POJO for the paging object */
public class ResponsePaging extends ApiData {
  private int count;
  private int offset;

  /**
   * Returns the response count
   *
   * @return response count
   */
  public int getCount() {
    return this.count;
  }

  /**
   * Returns the response offset
   *
   * @return response offset
   */
  public int getOffset() {
    return this.offset;
  }

  /** @return a stringified representation of ResponsePaging */
  @Override
  public String toString() {
    return this.getClass().getName() + "{" + "count=" + count + ", offset=" + offset + '}';
  }
}
