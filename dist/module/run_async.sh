script_dir=$(dirname "$(readlink -f "$0")")
export KB_DEPLOYMENT_CONFIG=$script_dir/deploy.cfg
WD=/kb/module/work
if [ -f $WD/token ]; then
    echo "Running async job..."
    cat $WD/token | xargs sh $script_dir/run_async_job.sh $WD/input.json $WD/output.json
else
    echo "File $WD/token doesn't exist, aborting."
    exit 1
fi
