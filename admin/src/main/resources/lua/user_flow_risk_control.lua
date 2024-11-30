-- 设置用户访问频率限制的参数
local username = KEYS[1]
local timeWindow = tonumber(ARGV[1]) -- 时间窗口，单位：秒

-- 构造 Redis 中存储用户访问次数的键名
local accessKey = "short-link:user-flow-risk-control:" .. username

-- 检查键是否已经存在
if redis.call("EXISTS", accessKey) == 1 then
    -- 如果键存在，直接递增访问次数，不更新过期时间
    return redis.call("INCR", accessKey)
else
    -- 如果键不存在，递增访问次数并设置过期时间
    redis.call("INCR", accessKey)
    redis.call("EXPIRE", accessKey, timeWindow)
    return 1  -- 第一次访问，返回访问次数 1
end