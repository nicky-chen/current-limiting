--
-- User: Nicky_chin
-- Date: 2017/12/18
-- Time: 15:39
--
local times = redis.call('incr', KEYS[1]) --设置key(KEY[1])并加1

if times == 1 then
    redis.call('expire', KEYS[1], ARGV[1]) --设置超时时间
end

if times > tonumber(ARGV[2]) then --限流大小
    return 0
end

return 1


