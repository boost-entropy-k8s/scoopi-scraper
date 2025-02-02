package org.codetab.scoopi.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class JobStateExceptionTest {

    private JobStateException ex;

    @Test
    public void testException() {
        String message = "xyz";

        ex = new JobStateException(message);

        assertThat(ex.getMessage()).isEqualTo(message);
    }

    @Test
    public void testCriticalExceptionWithCause() {
        Throwable cause = new Throwable("x");
        ex = new JobStateException(cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getMessage()).isNull();
    }

    @Test
    public void testCriticalExceptionWithMessageAndCause() {
        String message = "xyz";
        Throwable cause = new Throwable("x");

        ex = new JobStateException(message, cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getMessage()).isEqualTo(message);
    }

}
