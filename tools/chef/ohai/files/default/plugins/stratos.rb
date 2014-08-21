Ohai.plugin(:Stratos) do
  provides "stratos"

  collect_data do
    configs = File.read("/tmp/payload/launch-params").split(",").map(&:strip)

    configs.each { |x| key_value_pair = x.split("=").map(&:strip)
      #stratos["stratos_#{key_value_pair[0]}".to_sym] = key_value_pair[1].to_s
      stratos["stratos_instance_data_#{key_value_pair[0]}".to_sym] = key_value_pair[1].to_s
    }
  end
end