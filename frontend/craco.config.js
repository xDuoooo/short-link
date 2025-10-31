const path = require('path');

module.exports = {
  webpack: {
    configure: (webpackConfig, { env, paths }) => {
      if (env === 'development') {
        // 开发模式优化
        webpackConfig.optimization = {
          ...webpackConfig.optimization,
          removeAvailableModules: false,
          removeEmptyChunks: false,
          splitChunks: false,
        };
        
        // 禁用一些耗时的插件
        webpackConfig.plugins = webpackConfig.plugins.filter(plugin => {
          const pluginName = plugin.constructor.name;
          return !['ESLintWebpackPlugin', 'ForkTsCheckerWebpackPlugin'].includes(pluginName);
        });
        
        // 优化解析
        webpackConfig.resolve = {
          ...webpackConfig.resolve,
          symlinks: false,
          cacheWithContext: false,
        };
        
        // 优化模块解析
        webpackConfig.module.rules = webpackConfig.module.rules.map(rule => {
          if (rule.oneOf) {
            rule.oneOf = rule.oneOf.map(loader => {
              if (loader.test && loader.test.toString().includes('tsx?')) {
                return {
                  ...loader,
                  use: loader.use.map(use => {
                    if (typeof use === 'object' && use.loader && use.loader.includes('ts-loader')) {
                      return {
                        ...use,
                        options: {
                          ...use.options,
                          transpileOnly: true,
                          experimentalWatchApi: true,
                        }
                      };
                    }
                    return use;
                  })
                };
              }
              return loader;
            });
          }
          return rule;
        });
      }
      
      return webpackConfig;
    },
  },
  devServer: {
    hot: true,
    liveReload: true,
    // 新增代理配置
    proxy: {
      '/api': {
        target: 'http://localhost:8000',
        changeOrigin: true,
      }
    },
    watchFiles: {
      paths: ['src/**/*'],
      options: {
        usePolling: true,
        interval: 1000,
      },
    },
  },
};

