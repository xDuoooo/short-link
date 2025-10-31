// 全局请求去重工具
class RequestDeduplication {
  private static instance: RequestDeduplication;
  private pendingRequests: Map<string, Promise<any>> = new Map();
  private requestTimeouts: Map<string, NodeJS.Timeout> = new Map();

  static getInstance(): RequestDeduplication {
    if (!RequestDeduplication.instance) {
      RequestDeduplication.instance = new RequestDeduplication();
    }
    return RequestDeduplication.instance;
  }

  // 生成请求唯一标识
  private generateRequestKey(method: string, url: string, params?: any, data?: any): string {
    return `${method.toUpperCase()}-${url}-${JSON.stringify(params || {})}-${JSON.stringify(data || {})}`;
  }

  // 检查并处理重复请求
  async deduplicateRequest<T>(
    method: string,
    url: string,
    requestFn: () => Promise<T>,
    params?: any,
    data?: any,
    timeout: number = 30000
  ): Promise<T> {
    const requestKey = this.generateRequestKey(method, url, params, data);
    
    // 如果相同的请求正在进行，返回缓存的Promise
    if (this.pendingRequests.has(requestKey)) {
      console.log('检测到重复请求，返回缓存的Promise:', requestKey);
      return this.pendingRequests.get(requestKey)!;
    }

    // 创建新的请求Promise
    const requestPromise = requestFn()
      .then((result) => {
        // 请求成功，清理缓存
        this.cleanupRequest(requestKey);
        return result;
      })
      .catch((error) => {
        // 请求失败，清理缓存
        this.cleanupRequest(requestKey);
        throw error;
      });

    // 缓存请求Promise
    this.pendingRequests.set(requestKey, requestPromise);

    // 设置超时清理
    const timeoutId = setTimeout(() => {
      this.cleanupRequest(requestKey);
    }, timeout);
    this.requestTimeouts.set(requestKey, timeoutId);

    return requestPromise;
  }

  // 清理请求缓存
  private cleanupRequest(requestKey: string): void {
    this.pendingRequests.delete(requestKey);
    
    const timeoutId = this.requestTimeouts.get(requestKey);
    if (timeoutId) {
      clearTimeout(timeoutId);
      this.requestTimeouts.delete(requestKey);
    }
  }

  // 清理所有请求缓存
  clearAll(): void {
    this.pendingRequests.clear();
    this.requestTimeouts.forEach(timeoutId => clearTimeout(timeoutId));
    this.requestTimeouts.clear();
  }

  // 获取当前正在进行的请求数量
  getPendingRequestCount(): number {
    return this.pendingRequests.size;
  }

  // 获取当前正在进行的请求列表
  getPendingRequests(): string[] {
    return Array.from(this.pendingRequests.keys());
  }
}

export const requestDeduplication = RequestDeduplication.getInstance();
