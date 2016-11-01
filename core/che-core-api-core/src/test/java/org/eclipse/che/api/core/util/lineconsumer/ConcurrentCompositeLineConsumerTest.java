package org.eclipse.che.api.core.util.lineconsumer;

import org.eclipse.che.api.core.util.LineConsumer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author Mykola Morhun
 */
@Listeners(value = {MockitoTestNGListener.class})
public class ConcurrentCompositeLineConsumerTest  {

    @Mock
    private LineConsumer lineConsumer1;
    @Mock
    private LineConsumer lineConsumer2;
    @Mock
    private LineConsumer lineConsumer3;

    private ConcurrentCompositeLineConsumer concurrentCompositeLineConsumer;

    private LineConsumer subConsumers[];

    @BeforeMethod
    public void beforeMethod() throws Exception {
        subConsumers = new LineConsumer[] { lineConsumer1, lineConsumer2, lineConsumer3 };
        concurrentCompositeLineConsumer = new ConcurrentCompositeLineConsumer(subConsumers);
    }

    @Test
    public void shouldWriteMessageIntoEachConsumer() throws Exception {
        // given
        final String message = "Test line";

        // when
        concurrentCompositeLineConsumer.writeLine(message);

        // then
        for (LineConsumer subConsumer : subConsumers) {
            verify(subConsumer).writeLine(eq(message));
        }
    }

    @Test
    public void shouldNotWriteIntoSubConsumersAfterClosingCompositeConsumer() throws Exception {
        // given
        final String message = "Test line";

        // when
        concurrentCompositeLineConsumer.close();
        concurrentCompositeLineConsumer.writeLine(message);

        // then
        for (LineConsumer subConsumer : subConsumers) {
            verify(subConsumer, never()).writeLine(anyString());
        }
    }

    @DataProvider(name = "subConsumersExceptions")
    public Object[][] subConsumersExceptions() {
        return new Throwable[][] {
                {new ConsumerAlreadyClosedException("Error")},
                {new ClosedByInterruptException()}
        };
    }

    @Test(dataProvider = "subConsumersExceptions")
    public void shouldCloseSubConsumerOnException(Throwable exception) throws Exception {
        // given
        final String message = "Test line";
        final String message2 = "Test line2";

        LineConsumer closedConsumer = mock(LineConsumer.class);
        doThrow(exception).when(closedConsumer).writeLine(anyString());

        concurrentCompositeLineConsumer = new ConcurrentCompositeLineConsumer(appendTo(subConsumers, closedConsumer));

        // when
        concurrentCompositeLineConsumer.writeLine(message);
        concurrentCompositeLineConsumer.writeLine(message2);

        // then
        verify(closedConsumer, never()).writeLine(eq(message2));
        for (LineConsumer consumer : subConsumers) {
            verify(consumer).writeLine(eq(message2));
        }
    }

    @Test
    public void shouldDoNothingOnWriteLineIfAllSubConsumersAreClosed() throws Exception {
        // given
        final String message = "Test line";
        LineConsumer[] closedConsumers = subConsumers;
        for (LineConsumer consumer : closedConsumers) {
            doThrow(ConsumerAlreadyClosedException.class).when(consumer).writeLine(anyString());
        }
        concurrentCompositeLineConsumer = new ConcurrentCompositeLineConsumer(closedConsumers);

        // when
        concurrentCompositeLineConsumer.writeLine("Error");
        concurrentCompositeLineConsumer.writeLine(message);

        // then
        for (LineConsumer subConsumer : closedConsumers) {
            verify(subConsumer, never()).writeLine(eq(message));
        }
    }

    private LineConsumer[] appendTo(LineConsumer[] base, LineConsumer... toAppend ) {
        List<LineConsumer> allElements = new ArrayList<>(Arrays.asList(base));
        allElements.addAll(Arrays.asList(toAppend));
        return allElements.toArray(new LineConsumer[allElements.size()]);
    }

}
