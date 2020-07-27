local component = require("component")
local computer = component.computer
local eeprom = component.eeprom

local function fromhex(str)
    return (str:gsub("%s+", ""):gsub('..', function (cc)
        return string.char(tonumber(cc, 16))
    end))
end

local t = {}

local line
repeat
  line = io.read()
  if line then
    t[#t + 1] = line
  end
until not line

local s = table.concat(t, '')
local bytes = fromhex(s)

if #bytes > eeprom.getSize() then
  io.stderr:write("Data is too big for the eeprom!\n")
  return 1
end

eeprom.set(fromhex(s))
print("Checksum: " .. eeprom.getChecksum())
computer.beep(1000, 0.1)
