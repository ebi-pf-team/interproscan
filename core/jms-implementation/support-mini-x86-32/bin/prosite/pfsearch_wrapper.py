import subprocess
import sys
import os

# Get all profile files from the model directory
def get_profiles_from_model_dir(model_dir):
    profiles = []
    if not os.path.isdir(model_dir):
        sys.stderr.write(f"Error: model directory {model_dir} does not exist.\n")
        sys.exit(1)

    # List all .prf files in the model directory
    for filename in os.listdir(model_dir):
        if filename.endswith(".prf"):
            profiles.append(os.path.join(model_dir, filename))
    
    return profiles

# Run pfsearch binary on a sequence file and a profile
def run_pfsearch(profile, fasta_file, output_file, pfsearch_path, pfsearch_flags):
    command = [pfsearch_path, *pfsearch_flags, profile, fasta_file]

    output = subprocess.check_output(command, universal_newlines=True)
    if output.strip():
        with open(output_file, 'a') as out_file:
            out_file.write(output + '\n')


def run_pfsearch_on_all_models(fasta_file, model_dir, output_file, pfsearch_path, pfsearch_flags):
    profiles = get_profiles_from_model_dir(model_dir)
    if not profiles:
        sys.stderr.write(f"Error: No profile files found in {model_dir}\n")
        sys.exit(1)

    for profile in profiles:
        run_pfsearch(profile, fasta_file, output_file, pfsearch_path, pfsearch_flags)

if __name__ == "__main__":
    if len(sys.argv) < 4:
        sys.exit("Error: expected more than 4 arguments, check your command again")

    fasta_file = sys.argv[1]
    output_file = sys.argv[2]
    model_dir = sys.argv[3]
    pfsearch_path = sys.argv[4]
    pfsearch_flags = sys.argv[5:]

    #create the output file in case we don't have any matches
    open(output_file, 'a').close()

    # Run pfsearch on all models
    run_pfsearch_on_all_models(fasta_file, model_dir, output_file, pfsearch_path, pfsearch_flags)

    print(f"Completed.")
