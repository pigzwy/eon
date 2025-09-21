package com.eon.common.datasource.support;

import com.eon.common.datasource.annotation.UseDataSource;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DynamicDataSourceAspectTest {

    private DataSourceContextHolder contextHolder;

    @BeforeEach
    void setUp() {
        contextHolder = new DataSourceContextHolder();
    }

    @Test
    void shouldSwitchDataSourceWhenAvailable() {
        DynamicDataSourceAspect aspect = new DynamicDataSourceAspect(contextHolder, true, Set.of("slave"));
        TestService proxy = createProxy(new TestService(contextHolder), aspect);

        proxy.selectFromSlave();

        assertThat(proxy.getCapturedKey()).isEqualTo("slave");
        assertThat(contextHolder.peek()).isEmpty();
    }

    @Test
    void shouldSkipWhenKeyMissingInNonStrictMode() {
        DynamicDataSourceAspect aspect = new DynamicDataSourceAspect(contextHolder, false, Set.of("master"));
        TestService proxy = createProxy(new TestService(contextHolder), aspect);

        proxy.updateOnUnknown();

        assertThat(proxy.getCapturedKey()).isNull();
        assertThat(contextHolder.peek()).isEmpty();
    }

    @Test
    void shouldFailWhenKeyMissingInStrictMode() {
        DynamicDataSourceAspect aspect = new DynamicDataSourceAspect(contextHolder, true, Set.of("master"));
        TestService proxy = createProxy(new TestService(contextHolder), aspect);

        assertThatThrownBy(proxy::updateOnUnknown).isInstanceOf(IllegalStateException.class);
    }

    private TestService createProxy(TestService target, DynamicDataSourceAspect aspect) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        return factory.getProxy();
    }

    static class TestService {
        private final DataSourceContextHolder holder;
        private String capturedKey;

        TestService(DataSourceContextHolder holder) {
            this.holder = holder;
        }

        @UseDataSource("slave")
        public void selectFromSlave() {
            capturedKey = holder.peek().orElse(null);
        }

        @UseDataSource("unknown")
        public void updateOnUnknown() {
            capturedKey = holder.peek().orElse(null);
        }

        String getCapturedKey() {
            return capturedKey;
        }
    }
}
